package com.tracevault.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.Evidence;
import com.tracevault.backend.entity.InvestigationCase;
import com.tracevault.backend.entity.User;
import com.tracevault.backend.enms.EvidenceFormat;
import com.tracevault.backend.enms.IntegrityStatus;
import com.tracevault.backend.repository.EvidenceRepository;
import com.tracevault.backend.repository.InvestigationCaseRepository;
import com.tracevault.backend.repository.UserRepository;

@Service
public class EvidenceService {
    private final EvidenceRepository evidenceRepository;
    private final InvestigationCaseRepository caseRepository;
    private final UserRepository userRepository;

    public EvidenceService(EvidenceRepository evidenceRepository,
                           InvestigationCaseRepository caseRepository,
                           UserRepository userRepository) {
        this.evidenceRepository = evidenceRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Evidence registerEvidence(UUID caseId, UUID uploadedById, String fileName,
                                    EvidenceFormat format, String formatVersion,
                                    long fileSizeBytes, String storagePath, String sha256Hash) {
        if (fileSizeBytes <= 0 || fileSizeBytes > Evidence.MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size must be between 1 byte and 200 MB.");
        }
        if (sha256Hash == null || !sha256Hash.matches("(?i)[0-9a-f]{64}")) {
            throw new IllegalArgumentException("SHA-256 hash must contain exactly 64 hexadecimal characters.");
        }
        if (storagePath == null || storagePath.isBlank()) {
            throw new IllegalArgumentException("Storage path is required.");
        }

        InvestigationCase investigationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Investigation case not found."));
        User uploader = userRepository.findById(uploadedById)
                .orElseThrow(() -> new RuntimeException("User not found."));

        Evidence evidence = new Evidence();
        evidence.setInvestigationCase(investigationCase);
        evidence.setUploadedBy(uploader);
        evidence.setFileName(fileName);
        evidence.setFormat(format);
        evidence.setFormatVersion(formatVersion);
        evidence.setFileSizeBytes(fileSizeBytes);
        evidence.setStoragePath(storagePath);
        evidence.setSha256Hash(sha256Hash.toLowerCase());
        evidence.setIntegrityStatus(IntegrityStatus.PENDING);
        return evidenceRepository.save(evidence);
    }

    @Transactional(readOnly = true)
    public Evidence getEvidence(UUID evidenceId) {
        return evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found."));
    }

    @Transactional(readOnly = true)
    public List<Evidence> getEvidenceForCase(UUID caseId) {
        return evidenceRepository.findByInvestigationCase_CaseIdOrderByUploadedAtDesc(caseId);
    }

    @Transactional(readOnly = true)
    public List<Evidence> getEvidenceUploadedBy(UUID userId) {
        return evidenceRepository.findByUploadedBy_UserIdOrderByUploadedAtDesc(userId);
    }

    @Transactional
    public Evidence updateIntegrityStatus(UUID evidenceId, IntegrityStatus status) {
        Evidence evidence = getEvidence(evidenceId);
        evidence.setIntegrityStatus(status);
        return evidenceRepository.save(evidence);
    }

    public void deleteEvidence(UUID evidenceId) {
        evidenceRepository.delete(getEvidence(evidenceId));
    }
}
