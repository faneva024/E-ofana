package com.eofana.entite;
 
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.time.LocalDateTime;
 
/**
 
 *
 *  inscription peut être :
 *  - complète : l'apprenant a payé l'intégralité du prix (statutPaiement = COMPLETE)
 *  - partielle : l'apprenant a payé un acompte pour réserver sa place (statutPaiement = PARTIEL)
 *  - en attente : l'inscription est créée mais aucun paiement n'a encore été reçu
 *
 */
@Entity
@Table(name = "inscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inscription {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apprenant_id", nullable = false)
    private Apprenant apprenant;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formation_id", nullable = false)
    private Formation formation;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_paiement", nullable = false, length = 20)
    private StatutPaiement statutPaiement;
 
    @Column(name = "reservation_uniquement", nullable = false)
    private boolean reservationUniquement;
 
    @Column(name = "date_inscription", nullable = false)
    private LocalDateTime dateInscription;
 
    @Column(name = "numero_recu", unique = true, length = 50)
    private String numeroRecu;
 
    @Column(name = "operateur_paiement", length = 30)
    private String operateurPaiement;
 
    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;
 
    @PrePersist
    protected void avantCreation() {
        LocalDateTime maintenant = LocalDateTime.now();
        this.dateCreation = maintenant;
        if (this.dateInscription == null) {
            this.dateInscription = maintenant;
        }
        if (this.statutPaiement == null) {
            this.statutPaiement = StatutPaiement.EN_ATTENTE;
        }
    }
}
