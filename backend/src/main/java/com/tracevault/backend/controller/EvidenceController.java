package com.tracevault.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.Evidence;
import com.tracevault.backend.enms.EvidenceFormat;
import com.tracevault.backend.enms.IntegrityStatus;
import com.tracevault.backend.service.EvidenceService;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {
    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @PostMapping
    public Evidence registerEvidence(@RequestParam UUID caseId,
                                     @RequestParam UUID uploadedById,
                                     @RequestParam String fileName,
                                     @RequestParam EvidenceFormat format,
                                     @RequestParam(required = false) String formatVersion,
                                     @RequestParam long fileSizeBytes,
                                     @RequestParam String storagePath,
                                     @RequestParam String sha256Hash) {
        return evidenceService.registerEvidence(caseId, uploadedById, fileName, format,
                formatVersion, fileSizeBytes, storagePath, sha256Hash);
    }

    @GetMapping("/{evidenceId}")
    public Evidence getEvidence(@PathVariable UUID evidenceId) {
        return evidenceService.getEvidence(evidenceId);
    }

    @GetMapping("/case/{caseId}")
    public List<Evidence> getEvidenceForCase(@PathVariable UUID caseId) {
        return evidenceService.getEvidenceForCase(caseId);
    }

    @GetMapping("/uploader/{userId}")
    public List<Evidence> getEvidenceUploadedBy(@PathVariable UUID userId) {
        return evidenceService.getEvidenceUploadedBy(userId);
    }

    @PutMapping("/{evidenceId}/integrity-status")
    public Evidence updateIntegrityStatus(@PathVariable UUID evidenceId,
                                          @RequestParam IntegrityStatus status) {
        return evidenceService.updateIntegrityStatus(evidenceId, status);
    }

    @DeleteMapping("/{evidenceId}")
    public void deleteEvidence(@PathVariable UUID evidenceId) {
        evidenceService.deleteEvidence(evidenceId);
    }
}
