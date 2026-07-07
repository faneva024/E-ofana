package com.eofana.repository;

import com.eofana.entity.ValidationFormation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Dépôt Spring Data JPA pour ValidationFormation (audit de modération, T-B-101 / T-B-114).
 */
public interface ValidationFormationRepository extends JpaRepository<ValidationFormation, Long> {

    List<ValidationFormation> findByFormationIdFormationOrderByCreatedAtDesc(Long idFormation);
}
