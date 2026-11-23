package com.tracevault.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.AnalysisModuleScoreId;
import com.tracevault.backend.entity.ModuleScore;

public interface ModuleScoreRepository extends JpaRepository<ModuleScore, AnalysisModuleScoreId> {
    List<ModuleScore> findByAnalysis_AnalysisIdOrderById_ModuleAsc(UUID analysisId);
}
