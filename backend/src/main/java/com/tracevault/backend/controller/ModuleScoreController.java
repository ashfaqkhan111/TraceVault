package com.tracevault.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.ModuleScore;
import com.tracevault.backend.enms.AnalysisModule;
import com.tracevault.backend.service.ModuleScoreService;
import com.tracevault.backend.service.ModuleScoreService.ModuleScoreInput;

@RestController
@RequestMapping("/api/analyses/{analysisId}/module-scores")
public class ModuleScoreController {
    private final ModuleScoreService moduleScoreService;

    public ModuleScoreController(ModuleScoreService moduleScoreService) {
        this.moduleScoreService = moduleScoreService;
    }

    @PostMapping("/{module}")
    public ModuleScore saveScore(@PathVariable UUID analysisId,
                                 @PathVariable AnalysisModule module,
                                 @RequestParam int score) {
        return moduleScoreService.saveScore(analysisId, module, score);
    }

    @PostMapping
    public List<ModuleScore> saveScores(@PathVariable UUID analysisId,
                                        @RequestBody List<ModuleScoreInput> scores) {
        return moduleScoreService.saveScores(analysisId, scores);
    }

    @GetMapping
    public List<ModuleScore> getScores(@PathVariable UUID analysisId) {
        return moduleScoreService.getScores(analysisId);
    }
}
