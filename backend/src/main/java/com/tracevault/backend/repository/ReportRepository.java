package com.tracevault.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.Report;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    Optional<Report> findByReportCode(String reportCode);

    List<Report> findByAnalysis_AnalysisIdOrderByGeneratedAtDesc(UUID analysisId);

    boolean existsByReportCode(String reportCode);
}
