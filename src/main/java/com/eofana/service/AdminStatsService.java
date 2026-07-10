package com.eofana.service;

import com.eofana.enums.AbonnementType;
import com.eofana.enums.StatutFormation;
import com.eofana.repository.CentreRepository;
import com.eofana.repository.FormationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * T-B-212 : agrégation des statistiques admin (tableau de bord global,
 * T-B-209, et statistiques filtrées, T-B-210).
 *
 * Réutilise FinanceCalculationService pour tout ce qui est calcul
 * DE TAUX (7%/15%, cf. son commentaire de classe) ; ici, les montants
 * de commission sont déjà calculés à l'insertion par le trigger
 * fnCalculerCommission (module apprenant) et simplement AGRÉGÉS (SUM)
 * — ce n'est pas une nouvelle logique de calcul de commission, juste
 * une lecture, exactement comme le fait la vue SQL de référence
 * "vStatsAdmin".
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final FormationRepository formationRepository;
    private final CentreRepository centreRepository;
    private final NamedParameterJdbcTemplate jdbc;

    /** T-B-209 : résumé global pour AdminDashboardController#getGlobalStats. */
    public GlobalStats getGlobalStats() {
        long formationsApprouvees = formationRepository.countByStatut(StatutFormation.approuve);
        long formationsEnAttente = formationRepository.countByStatut(StatutFormation.enAttente);
        long formationsRejetees = formationRepository.countByStatut(StatutFormation.rejete);

        Long nbApprenantsInscrits = jdbc.queryForObject("""
                SELECT COUNT(DISTINCT i."idUser")
                FROM eofana.inscriptions i
                WHERE i.statut = 'valide'
                """, Map.of(), Long.class);

        Long commissionsTotales = jdbc.queryForObject("""
                SELECT COALESCE(SUM(i."commission"), 0)
                FROM eofana.inscriptions i
                WHERE i.statut = 'valide'
                """, Map.of(), Long.class);

        // Revenu abonnements : prix mensuel selon le type (basic=60 000 Ar,
        // premium=200 000 Ar, cf. cahier des charges §3.2) x nombre de
        // mois écoulés depuis dateDebut (ou jusqu'à dateFin si clôturé).
        Long revenuAbonnements = jdbc.queryForObject("""
                SELECT COALESCE(SUM(
                    (CASE a."type" WHEN 'premium' THEN 200000 ELSE 60000 END)
                    * GREATEST(1, EXTRACT(YEAR FROM AGE(COALESCE(a."dateFin", CURRENT_DATE), a."dateDebut")) * 12
                              + EXTRACT(MONTH FROM AGE(COALESCE(a."dateFin", CURRENT_DATE), a."dateDebut")))
                ), 0)::BIGINT
                FROM eofana.abonnements a
                """, Map.of(), Long.class);

        long chiffreAffairesTotal = (commissionsTotales != null ? commissionsTotales : 0)
                + (revenuAbonnements != null ? revenuAbonnements : 0);

        List<EvolutionMensuelle> evolution = getEvolutionMensuelle(12);

        return new GlobalStats(
                formationsApprouvees, formationsEnAttente, formationsRejetees,
                nbApprenantsInscrits != null ? nbApprenantsInscrits : 0,
                chiffreAffairesTotal,
                commissionsTotales != null ? commissionsTotales : 0,
                revenuAbonnements != null ? revenuAbonnements : 0,
                evolution
        );
    }

    /** Évolution mensuelle (inscriptions + revenus) sur les N derniers mois glissants. */
    private List<EvolutionMensuelle> getEvolutionMensuelle(int nbMois) {
        List<Map<String, Object>> lignes = jdbc.queryForList("""
                SELECT to_char(date_trunc('month', i."createdAt"), 'YYYY-MM') AS mois,
                       COUNT(*) AS nbInscriptions,
                       COALESCE(SUM(i."commission"), 0) AS revenus
                FROM eofana.inscriptions i
                WHERE i.statut = 'valide'
                  AND i."createdAt" >= :depuis
                GROUP BY date_trunc('month', i."createdAt")
                ORDER BY date_trunc('month', i."createdAt")
                """, Map.of("depuis", YearMonth.now().minusMonths(nbMois - 1L).atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC)));

        return lignes.stream()
                .map(l -> new EvolutionMensuelle(
                        (String) l.get("mois"),
                        ((Number) l.get("nbinscriptions")).longValue(),
                        ((Number) l.get("revenus")).longValue()
                ))
                .toList();
    }

    /**
     * T-B-210 : statistiques filtrables, tous les filtres sont
     * optionnels et combinables (ex. catégorie + période) — même
     * principe que CentreRepository#rechercherFormateurs.
     */
    public FilteredStats getFilteredStats(StatsFilterDto filtres) {
        List<String> conditions = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource();

        String base = """
                SELECT COUNT(DISTINCT i."idInscription") AS nbInscriptions,
                       COALESCE(SUM(i."montantPaye"), 0) AS revenusBruts,
                       COALESCE(SUM(i."commission"), 0) AS commissions
                FROM eofana.inscriptions i
                JOIN eofana."sessionsFormation" s ON s."idSession" = i."idSession"
                JOIN eofana.formations f ON f."idFormation" = s."idFormation"
                JOIN eofana.centres c ON c."idCentre" = f."idCentre"
                WHERE i.statut = 'valide'
                """;

        if (filtres.idCentre() != null) {
            conditions.add("c.\"idCentre\" = :idCentre");
            params.addValue("idCentre", filtres.idCentre());
        }
        if (filtres.idCategorie() != null) {
            conditions.add("f.\"idCategorie\" = :idCategorie");
            params.addValue("idCategorie", filtres.idCategorie());
        }
        if (filtres.ville() != null && !filtres.ville().isBlank()) {
            conditions.add("LOWER(c.\"ville\") = LOWER(:ville)");
            params.addValue("ville", filtres.ville());
        }
        if (filtres.abonnement() != null) {
            conditions.add("c.\"abonnement\" = :abonnement");
            params.addValue("abonnement", filtres.abonnement().name());
        }
        if (filtres.periodeDebut() != null) {
            conditions.add("i.\"createdAt\" >= :periodeDebut");
            params.addValue("periodeDebut", filtres.periodeDebut().atStartOfDay());
        }
        if (filtres.periodeFin() != null) {
            conditions.add("i.\"createdAt\" < :periodeFin");
            params.addValue("periodeFin", filtres.periodeFin().plusDays(1).atStartOfDay());
        }

        String sql = base + (conditions.isEmpty() ? "" : " AND " + String.join(" AND ", conditions));

        Map<String, Object> resultat = jdbc.queryForMap(sql, params);

        long nbFormationsConcernees = filtres.idCategorie() != null
                ? formationRepository.countByStatutAndCategorie_IdCategorie(StatutFormation.approuve, filtres.idCategorie())
                : formationRepository.countByStatut(StatutFormation.approuve);

        return new FilteredStats(
                nbFormationsConcernees,
                ((Number) resultat.get("nbinscriptions")).longValue(),
                ((Number) resultat.get("revenusbruts")).longValue(),
                ((Number) resultat.get("commissions")).longValue()
        );
    }

    public record GlobalStats(
            long formationsApprouvees,
            long formationsEnAttente,
            long formationsRejetees,
            long nbApprenantsInscrits,
            long chiffreAffairesTotal,
            long commissionsTotales,
            long revenuAbonnements,
            List<EvolutionMensuelle> evolutionMensuelle
    ) {
    }

    public record EvolutionMensuelle(String mois, long nbInscriptions, long revenus) {
    }

    public record FilteredStats(long nbFormations, long nbInscriptions, long revenusBruts, long commissions) {
    }

    /** Tous les filtres sont optionnels (null autorisé) et combinables. */
    public record StatsFilterDto(
            Long idCentre,
            Long idCategorie,
            String ville,
            AbonnementType abonnement,
            LocalDate periodeDebut,
            LocalDate periodeFin
    ) {
    }
}
