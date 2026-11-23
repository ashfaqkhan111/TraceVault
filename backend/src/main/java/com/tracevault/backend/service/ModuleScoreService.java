package com.tracevault.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.AnalysisModuleScoreId;
import com.tracevault.backend.entity.ModuleScore;
import com.tracevault.backend.enms.AnalysisModule;
import com.tracevault.backend.repository.AnalysisRepository;
import com.tracevault.backend.repository.ModuleScoreRepository;

@Service
public class ModuleScoreService {
    private final ModuleScoreRepository moduleScoreRepository;
    private final AnalysisRepository analysisRepository;

    public ModuleScoreService(ModuleScoreRepository moduleScoreRepository,
                              AnalysisRepository analysisRepository) {
        this.moduleScoreRepository = moduleScoreRepository;
        this.analysisRepository = analysisRepository;
    }

    @Transactional
    public ModuleScore saveScore(UUID analysisId, AnalysisModule module, int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Module score must be between 0 and 100.");
        }
        var analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found."));
        ModuleScore moduleScore = moduleScoreRepository
                .findById(new AnalysisModuleScoreId(analysisId, module))
                .orElseGet(() -> new ModuleScore(analysis, module, score));
        moduleScore.setScore(score);
        return moduleScoreRepository.save(moduleScore);
    }

    @Transactional
    public List<ModuleScore> saveScores(UUID analysisId, List<ModuleScoreInput> scores) {
        var analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found."));
        List<ModuleScore> moduleScores = scores.stream().map(input -> {
            if (input.score() < 0 || input.score() > 100) {
                throw new IllegalArgumentException("Module score must be between 0 and 100.");
            }
            ModuleScore moduleScore = moduleScoreRepository
                    .findById(new AnalysisModuleScoreId(analysisId, input.module()))
                    .orElseGet(() -> new ModuleScore(analysis, input.module(), input.score()));
            moduleScore.setScore(input.score());
            return moduleScore;
        }).toList();
        return moduleScoreRepository.saveAll(moduleScores);
    }

    @Transactional(readOnly = true)
    public List<ModuleScore> getScores(UUID analysisId) {
        return moduleScoreRepository.findByAnalysis_AnalysisIdOrderById_ModuleAsc(analysisId);
    }

    public record ModuleScoreInput(AnalysisModule module, int score) {}
}
