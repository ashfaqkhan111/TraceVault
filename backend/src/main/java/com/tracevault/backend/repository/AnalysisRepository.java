package com.tracevault.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.Analysis;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {
    List<Analysis> findByEvidence_EvidenceIdOrderByStartedAtDesc(UUID evidenceId);

    List<Analysis> findByRequestedBy_UserIdOrderByStartedAtDesc(UUID userId);
}
