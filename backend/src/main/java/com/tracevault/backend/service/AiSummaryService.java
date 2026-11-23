package com.tracevault.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.AiSummary;
import com.tracevault.backend.repository.AiSummaryRepository;
import com.tracevault.backend.repository.AnalysisRepository;
import com.tracevault.backend.repository.UserRepository;

@Service
public class AiSummaryService {
    private final AiSummaryRepository summaryRepository;
    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;

    public AiSummaryService(AiSummaryRepository summaryRepository,
                            AnalysisRepository analysisRepository,
                            UserRepository userRepository) {
        this.summaryRepository = summaryRepository;
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AiSummary create(UUID analysisId, AiSummaryInput input) {
        var analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found."));
        if (summaryRepository.findByAnalysis_AnalysisId(analysisId).isPresent()) {
            throw new IllegalArgumentException("An AI summary already exists for this analysis.");
        }
        validateConfidence(input.confidencePct());

        AiSummary summary = new AiSummary();
        summary.setAnalysis(analysis);
        summary.setSummaryText(input.summaryText());
        summary.setKeyRisks(input.keyRisks() == null ? List.of() : input.keyRisks());
        summary.setConfidencePct(input.confidencePct());
        summary.setFlowId(input.flowId());
        return summaryRepository.save(summary);
    }

    @Transactional(readOnly = true)
    public AiSummary getByAnalysis(UUID analysisId) {
        return summaryRepository.findByAnalysis_AnalysisId(analysisId)
                .orElseThrow(() -> new RuntimeException("AI summary not found."));
    }

    @Transactional
    public AiSummary review(UUID analysisId, UUID reviewerId) {
        AiSummary summary = getByAnalysis(analysisId);
        var reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found."));
        summary.setReviewedBy(reviewer);
        return summaryRepository.save(summary);
    }

    private void validateConfidence(int confidencePct) {
        if (confidencePct < 0 || confidencePct > 100) {
            throw new IllegalArgumentException("Confidence must be between 0 and 100.");
        }
    }

    public record AiSummaryInput(String summaryText, List<String> keyRisks,
                                int confidencePct, String flowId) {}
}
