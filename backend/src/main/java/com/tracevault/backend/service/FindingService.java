package com.tracevault.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.Finding;
import com.tracevault.backend.enms.FindingModule;
import com.tracevault.backend.enms.FindingSeverity;
import com.tracevault.backend.repository.AnalysisRepository;
import com.tracevault.backend.repository.FindingRepository;

@Service
public class FindingService {
    private final FindingRepository findingRepository;
    private final AnalysisRepository analysisRepository;

    public FindingService(FindingRepository findingRepository, AnalysisRepository analysisRepository) {
        this.findingRepository = findingRepository;
        this.analysisRepository = analysisRepository;
    }

    @Transactional
    public Finding create(UUID analysisId, FindingInput input) {
        var analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found."));
        Finding finding = new Finding();
        finding.setAnalysis(analysis);
        finding.setSeverity(input.severity());
        finding.setTitle(input.title());
        finding.setDescription(input.description());
        finding.setModule(input.module());
        finding.setEvidenceRef(input.evidenceRef());
        return findingRepository.save(finding);
    }

    @Transactional(readOnly = true)
    public List<Finding> getForAnalysis(UUID analysisId) {
        return findingRepository.findByAnalysis_AnalysisIdOrderBySeverityAsc(analysisId);
    }

    @Transactional(readOnly = true)
    public Finding get(UUID analysisId, UUID findingId) {
        Finding finding = findingRepository.findById(findingId)
                .orElseThrow(() -> new RuntimeException("Finding not found."));
        if (!finding.getAnalysis().getAnalysisId().equals(analysisId)) {
            throw new RuntimeException("Finding does not belong to this analysis.");
        }
        return finding;
    }

    @Transactional
    public Finding update(UUID analysisId, UUID findingId, FindingInput input) {
        Finding finding = get(analysisId, findingId);
        finding.setSeverity(input.severity());
        finding.setTitle(input.title());
        finding.setDescription(input.description());
        finding.setModule(input.module());
        finding.setEvidenceRef(input.evidenceRef());
        return findingRepository.save(finding);
    }

    @Transactional
    public void delete(UUID analysisId, UUID findingId) {
        findingRepository.delete(get(analysisId, findingId));
    }

    public record FindingInput(FindingSeverity severity, String title, String description,
                               FindingModule module, String evidenceRef) {}
}
