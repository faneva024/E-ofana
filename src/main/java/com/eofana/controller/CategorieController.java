package com.eofana.controller;

import com.eofana.entity.Categorie;
import com.eofana.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class CategorieController {

    private final CategorieRepository categorieRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<Categorie> getAllCategories() {
        return categorieRepository.findAll();
    }

    @PostMapping
    @Transactional
    public Categorie creerCategorie(@RequestBody CategorieRequest request) {
        Categorie categorie = Categorie.builder()
                .nom(request.nom())
                .description(request.description())
                .icone(request.icone() != null ? request.icone() : "bi-book")
                .couleur(request.couleur() != null ? request.couleur() : "#2E75B6")
                .build();

        return categorieRepository.save(categorie);
    }

    public record CategorieRequest(
            String nom,
            String description,
            String icone,
            String couleur
    ) {
    }
}