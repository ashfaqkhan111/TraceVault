package com.tracevault.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.Finding;

public interface FindingRepository extends JpaRepository<Finding, UUID> {
    List<Finding> findByAnalysis_AnalysisIdOrderBySeverityAsc(UUID analysisId);
}
