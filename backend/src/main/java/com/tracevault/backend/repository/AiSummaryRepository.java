package com.tracevault.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.AiSummary;

public interface AiSummaryRepository extends JpaRepository<AiSummary, UUID> {
    Optional<AiSummary> findByAnalysis_AnalysisId(UUID analysisId);
}
