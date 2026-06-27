package com.eofana.entite;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Utilisateur — alignée sur la table "utilisateurs" de eofanaDb v3.0
 * Couvre tous les rôles : apprenant, formateur, moderateur, commercial, admin.
 * Le champ "role" détermine l'espace et les droits dans la plateforme.
 */
@Entity
@Table(name = "\"utilisateurs\"")
@EntityListeners(AuditingEntityListener.class)
public class Utilisateur {

    // ── Énumération des rôles (miroir du type PostgreSQL "roleUtilisateur") ──
    public enum Role {
        apprenant, formateur, moderateur, commercial, admin
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idUser\"")
    private Long idUser;

    @Column(name = "\"uuid\"", nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @NotBlank(message = "Le nom est obligatoire.")
    @Size(max = 100)
    @Column(name = "\"nom\"", nullable = false, length = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire.")
    @Size(max = 100)
    @Column(name = "\"prenom\"", nullable = false, length = 100)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "Format d'email invalide.")
    @Size(max = 180)
    @Column(name = "\"email\"", nullable = false, unique = true, length = 180)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
    @Column(name = "\"motDePasse\"", nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "\"role\"", nullable = false)
    private Role role = Role.apprenant;

    @Size(max = 20)
    @Column(name = "\"telephone\"", length = 20)
    private String telephone;

    @Column(name = "\"avatar\"")
    private String avatar;

    @Column(name = "\"estActif\"", nullable = false)
    private boolean estActif = true;

    @CreatedDate
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "\"updatedAt\"", nullable = false)
    private LocalDateTime updatedAt;

    // ── Constructeurs ────────────────────────────────────────────────────────
    public Utilisateur() {}

    public Utilisateur(String nom, String prenom, String email, String motDePasse) {
        this.nom        = nom;
        this.prenom     = prenom;
        this.email      = email;
        this.motDePasse = motDePasse;
    }

    // ── Méthodes utilitaires ─────────────────────────────────────────────────
    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public boolean estApprenant()   { return Role.apprenant.equals(this.role); }
    public boolean estFormateur()   { return Role.formateur.equals(this.role); }
    public boolean estAdmin()       { return Role.admin.equals(this.role); }
    public boolean estModerateur()  { return Role.moderateur.equals(this.role); }

    // ── Getters / Setters ────────────────────────────────────────────────────
    public Long getIdUser()                   { return idUser; }
    public void setIdUser(Long idUser)        { this.idUser = idUser; }
    public UUID getUuid()                     { return uuid; }
    public void setUuid(UUID uuid)            { this.uuid = uuid; }
    public String getNom()                    { return nom; }
    public void setNom(String nom)            { this.nom = nom; }
    public String getPrenom()                 { return prenom; }
    public void setPrenom(String prenom)      { this.prenom = prenom; }
    public String getEmail()                  { return email; }
    public void setEmail(String email)        { this.email = email; }
    public String getMotDePasse()             { return motDePasse; }
    public void setMotDePasse(String mdp)     { this.motDePasse = mdp; }
    public Role getRole()                     { return role; }
    public void setRole(Role role)            { this.role = role; }
    public String getTelephone()              { return telephone; }
    public void setTelephone(String tel)      { this.telephone = tel; }
    public String getAvatar()                 { return avatar; }
    public void setAvatar(String avatar)      { this.avatar = avatar; }
    public boolean isEstActif()               { return estActif; }
    public void setEstActif(boolean estActif) { this.estActif = estActif; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public LocalDateTime getUpdatedAt()       { return updatedAt; }
}
