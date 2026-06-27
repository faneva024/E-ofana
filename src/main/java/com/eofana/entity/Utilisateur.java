package com.eofana.entity;

import com.eofana.enums.RoleUtilisateur;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "\"utilisateurs\"")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idUser\"")
    private Long idUser;

    @UuidGenerator
    @Column(name = "\"uuid\"", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    @Column(name = "\"nom\"", nullable = false, length = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    @Column(name = "\"prenom\"", nullable = false, length = 100)
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    @Size(max = 180, message = "L'email ne doit pas dépasser 180 caractères")
    @Column(name = "\"email\"", nullable = false, unique = true, length = 180)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(max = 255, message = "Le mot de passe haché ne doit pas dépasser 255 caractères")
    @Column(name = "\"motDePasse\"", nullable = false, length = 255)
    private String motDePasse;

    @NotNull(message = "Le rôle est obligatoire")
    @Enumerated(EnumType.STRING) // AJOUT: Spécifie que c'est un Enum
    @Column(name = "\"role\"", nullable = false, columnDefinition = "VARCHAR(50)") // AJOUT
    @Builder.Default
    private RoleUtilisateur role = RoleUtilisateur.apprenant;

    @Size(max = 20, message = "Le téléphone ne doit pas dépasser 20 caractères")
    @Column(name = "\"telephone\"", length = 20)
    private String telephone;

    @Size(max = 255, message = "Le chemin de l'avatar ne doit pas dépasser 255 caractères")
    @Column(name = "\"avatar\"", length = 255)
    private String avatar;

    @Column(name = "\"estActif\"", nullable = false)
    @Builder.Default
    private Boolean estActif = true;

    @CreatedDate
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "\"updatedAt\"", nullable = false)
    private OffsetDateTime updatedAt;
}