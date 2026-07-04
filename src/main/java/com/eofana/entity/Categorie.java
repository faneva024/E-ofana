package com.eofana.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA mappée sur la table "categories" (schéma eofana).
 *
 * Référentiel des thématiques de formations (Informatique, Langues,
 * Gestion, Mécanique…). Correspond à la migration V4 (qui crée
 * categories, centres et formations ensemble).
 */
@Entity
@Table(name = "\"categories\"") 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "\"idCategorie\"") 
    private Long idCategorie;

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    @Column(name = "\"nom\"", nullable = false, unique = true, length = 100) 
    private String nom;

    @Size(max = 300, message = "La description ne doit pas dépasser 300 caractères")
    @Column(name = "\"description\"", length = 300)
    private String description;

    @Column(name = "\"icone\"", length = 50)
    @Builder.Default
    private String icone = "bi-book";

    @Column(name = "\"couleur\"", length = 20)
    @Builder.Default
    private String couleur = "#2E75B6";
}