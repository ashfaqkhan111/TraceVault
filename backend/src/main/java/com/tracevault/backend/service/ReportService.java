package com.tracevault.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.Report;
import com.tracevault.backend.repository.AnalysisRepository;
import com.tracevault.backend.repository.ReportRepository;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final AnalysisRepository analysisRepository;

    public ReportService(ReportRepository reportRepository, AnalysisRepository analysisRepository) {
        this.reportRepository = reportRepository;
        this.analysisRepository = analysisRepository;
    }

    @Transactional
    public Report create(UUID analysisId, ReportInput input) {
        var analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found."));
        if (reportRepository.existsByReportCode(input.reportCode())) {
            throw new IllegalArgumentException("Report code already exists.");
        }

        Report report = new Report();
        report.setAnalysis(analysis);
        report.setReportCode(input.reportCode());
        report.setLimitations(input.limitations() == null ? List.of() : input.limitations());
        report.setPdfPath(input.pdfPath());
        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public Report getById(UUID reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found."));
    }

    @Transactional(readOnly = true)
    public List<Report> getByAnalysis(UUID analysisId) {
        return reportRepository.findByAnalysis_AnalysisIdOrderByGeneratedAtDesc(analysisId);
    }

    @Transactional
    public Report recordEmailDelivery(UUID reportId, String emailedTo) {
        Report report = getById(reportId);
        report.setEmailedTo(emailedTo);
        report.setEmailedAt(OffsetDateTime.now());
        return reportRepository.save(report);
    }

    public record ReportInput(String reportCode, List<String> limitations, String pdfPath) {}
}
