package com.tracevault.backend.controller;

import java.util.UUID;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.Report;
import com.tracevault.backend.service.ReportService;
import com.tracevault.backend.service.ReportService.ReportInput;

@RestController
@RequestMapping("/api")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/analyses/{analysisId}/report")
    public Report create(@PathVariable UUID analysisId, @RequestBody ReportInput input) {
        return reportService.create(analysisId, input);
    }

    @GetMapping("/analyses/{analysisId}/report")
    public List<Report> getByAnalysis(@PathVariable UUID analysisId) {
        return reportService.getByAnalysis(analysisId);
    }

    @GetMapping("/reports/{reportId}")
    public Report getById(@PathVariable UUID reportId) {
        return reportService.getById(reportId);
    }

    @PostMapping("/reports/{reportId}/email-delivery")
    public Report recordEmailDelivery(@PathVariable UUID reportId,
                                      @RequestParam String emailedTo) {
        return reportService.recordEmailDelivery(reportId, emailedTo);
    }
}
