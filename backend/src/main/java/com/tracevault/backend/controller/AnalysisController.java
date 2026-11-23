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

import com.tracevault.backend.entity.Analysis;
import com.tracevault.backend.enms.AnalysisType;
import com.tracevault.backend.service.AnalysisService;
import com.tracevault.backend.service.AnalysisService.AnalysisResultInput;

@RestController
@RequestMapping("/api")
public class AnalysisController {
    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/evidence/{evidenceId}/analyses")
    public Analysis requestAnalysis(@PathVariable UUID evidenceId,
                                    @RequestParam UUID requestedById,
                                    @RequestParam AnalysisType analysisType) {
        return analysisService.requestAnalysis(evidenceId, requestedById, analysisType);
    }

    @GetMapping("/evidence/{evidenceId}/analyses")
    public List<Analysis> getEvidenceHistory(@PathVariable UUID evidenceId) {
        return analysisService.getEvidenceHistory(evidenceId);
    }

    @GetMapping("/analyses/{analysisId}")
    public Analysis getAnalysis(@PathVariable UUID analysisId) {
        return analysisService.getAnalysis(analysisId);
    }

    @PostMapping("/analyses/{analysisId}/complete")
    public Analysis completeAnalysis(@PathVariable UUID analysisId,
                                     @RequestBody AnalysisResultInput result) {
        return analysisService.completeAnalysis(analysisId, result);
    }

    @PostMapping("/analyses/{analysisId}/fail")
    public Analysis failAnalysis(@PathVariable UUID analysisId) {
        return analysisService.failAnalysis(analysisId);
    }
}
