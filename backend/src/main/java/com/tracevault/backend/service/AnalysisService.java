package com.tracevault.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.Analysis;
import com.tracevault.backend.enms.AnalysisStatus;
import com.tracevault.backend.enms.AnalysisType;
import com.tracevault.backend.enms.RiskLabel;
import com.tracevault.backend.repository.AnalysisRepository;
import com.tracevault.backend.repository.EvidenceRepository;
import com.tracevault.backend.repository.UserRepository;

@Service
public class AnalysisService {
    private final AnalysisRepository analysisRepository;
    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;

    public AnalysisService(AnalysisRepository analysisRepository,
                           EvidenceRepository evidenceRepository,
                           UserRepository userRepository) {
        this.analysisRepository = analysisRepository;
        this.evidenceRepository = evidenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Analysis requestAnalysis(UUID evidenceId, UUID requestedById, AnalysisType analysisType) {
        var evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found."));
        var requester = userRepository.findById(requestedById)
                .orElseThrow(() -> new RuntimeException("User not found."));

        Analysis analysis = new Analysis();
        analysis.setEvidence(evidence);
        analysis.setRequestedBy(requester);
        analysis.setAnalysisType(analysisType);
        analysis.setStatus(AnalysisStatus.PROCESSING);
        return analysisRepository.save(analysis);
    }

    @Transactional(readOnly = true)
    public Analysis getAnalysis(UUID analysisId) {
        return analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found."));
    }

    @Transactional(readOnly = true)
    public List<Analysis> getEvidenceHistory(UUID evidenceId) {
        return analysisRepository.findByEvidence_EvidenceIdOrderByStartedAtDesc(evidenceId);
    }

    @Transactional(readOnly = true)
    public List<Analysis> getRequestedBy(UUID userId) {
        return analysisRepository.findByRequestedBy_UserIdOrderByStartedAtDesc(userId);
    }

    @Transactional
    public Analysis completeAnalysis(UUID analysisId, AnalysisResultInput result) {
        Analysis analysis = getAnalysis(analysisId);
        if (analysis.getStatus() != AnalysisStatus.PROCESSING) {
            throw new IllegalStateException("Only processing analyses can be completed.");
        }
        if (result.riskScore() < 0 || result.riskScore() > 100) {
            throw new IllegalArgumentException("Risk score must be between 0 and 100.");
        }
        if (result.checksCount() < 0) {
            throw new IllegalArgumentException("Checks count cannot be negative.");
        }
        analysis.setRiskScore(result.riskScore());
        analysis.setRiskLabel(result.riskLabel());
        analysis.setHashVerified(result.hashVerified());
        analysis.setChecksCount(result.checksCount());
        analysis.setStatus(AnalysisStatus.COMPLETED);
        analysis.setCompletedAt(OffsetDateTime.now());
        return analysisRepository.save(analysis);
    }

    @Transactional
    public Analysis failAnalysis(UUID analysisId) {
        Analysis analysis = getAnalysis(analysisId);
        if (analysis.getStatus() != AnalysisStatus.PROCESSING) {
            throw new IllegalStateException("Only processing analyses can be failed.");
        }
        analysis.setStatus(AnalysisStatus.FAILED);
        analysis.setCompletedAt(OffsetDateTime.now());
        return analysisRepository.save(analysis);
    }

    public record AnalysisResultInput(int riskScore, RiskLabel riskLabel,
                                     boolean hashVerified, int checksCount) {}
}
