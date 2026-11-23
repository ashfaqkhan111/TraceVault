package com.tracevault.backend.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.AiSummary;
import com.tracevault.backend.service.AiSummaryService;
import com.tracevault.backend.service.AiSummaryService.AiSummaryInput;

@RestController
@RequestMapping("/api/analyses/{analysisId}/ai-summary")
public class AiSummaryController {
    private final AiSummaryService summaryService;

    public AiSummaryController(AiSummaryService summaryService) {
        this.summaryService = summaryService;
    }

    @PostMapping
    public AiSummary create(@PathVariable UUID analysisId, @RequestBody AiSummaryInput input) {
        return summaryService.create(analysisId, input);
    }

    @GetMapping
    public AiSummary getByAnalysis(@PathVariable UUID analysisId) {
        return summaryService.getByAnalysis(analysisId);
    }

    @PostMapping("/review")
    public AiSummary review(@PathVariable UUID analysisId, @RequestParam UUID reviewerId) {
        return summaryService.review(analysisId, reviewerId);
    }
}
