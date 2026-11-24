package com.tracevault.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.MetadataField;

public interface MetadataFieldRepository extends JpaRepository<MetadataField, UUID> {
    List<MetadataField> findByEvidence_EvidenceIdOrderByFieldKeyAsc(UUID evidenceId);

    List<MetadataField> findByEvidence_EvidenceIdAndAnomalyTrueOrderByFieldKeyAsc(UUID evidenceId);

    long countByEvidence_EvidenceId(UUID evidenceId);

    long countByEvidence_EvidenceIdAndAnomalyTrue(UUID evidenceId);
}
