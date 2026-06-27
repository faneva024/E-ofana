package com.eofana.controller;

import com.eofana.entity.Formation;
import com.eofana.repository.FormationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/formations")
@CrossOrigin(origins = "*")
public class ControleurFormation {

    private final FormationRepository formationRepository;

    public ControleurFormation(FormationRepository formationRepository) {
        this.formationRepository = formationRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Formation>> listerFormations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille,
            @RequestParam(required = false) String categorie) {

        Pageable pageable = PageRequest.of(page, taille);
        Page<Formation> formations;

        if (categorie != null && !categorie.isBlank()) {
            formations = formationRepository.findByStatutAndCategorie("approuve", categorie, pageable);
        } else {
            formations = formationRepository.findByStatut("approuve", pageable);
        }
        return ResponseEntity.ok(formations);
    }

    @GetMapping("/{idFormation}")
    public ResponseEntity<?> detailFormation(@PathVariable Long idFormation) {
        return formationRepository.findById(idFormation)
                .filter(f -> "approuve".equals(f.getStatut()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/rechercher")
    public ResponseEntity<Page<Formation>> rechercherFormations(
            @RequestParam(required = false) String motsCles,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String lieu,
            @RequestParam(required = false) BigDecimal prixMin,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {

        Pageable pageable = PageRequest.of(page, taille);
        Page<Formation> resultats = formationRepository.rechercherFormations(
                motsCles, categorie, lieu, prixMin, prixMax, pageable);
        return ResponseEntity.ok(resultats);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> listerCategories() {
        List<String> categories = formationRepository.findAllCategories();
        return ResponseEntity.ok(categories);
    }
}
