package com.tracevault.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final Path storageDirectory;

    public EvidenceService(EvidenceRepository evidenceRepository,
                           InvestigationCaseRepository caseRepository,
                           UserRepository userRepository,
                           @Value("${tracevault.evidence.storage-dir:${java.io.tmpdir}/tracevault-evidence}") String storageDirectory) {
        this.evidenceRepository = evidenceRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Transactional
    public Evidence uploadEvidence(UUID caseId, UUID uploadedById, MultipartFile file,
                                   EvidenceFormat format, String formatVersion) {
        if (file == null || file.isEmpty() || file.getSize() <= 0
                || file.getSize() > Evidence.MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File size must be between 1 byte and 200 MB.");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Uploaded file must have a file name.");
        }
        String safeFileName = Path.of(originalName).getFileName().toString();
        String extension = safeFileName.contains(".")
                ? safeFileName.substring(safeFileName.lastIndexOf('.') + 1).toUpperCase()
                : "";
        if (!extension.equals(format.name())) {
            throw new IllegalArgumentException("Uploaded file extension does not match the selected format.");
        }

        InvestigationCase investigationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Investigation case not found."));
        User uploader = userRepository.findById(uploadedById)
                .orElseThrow(() -> new RuntimeException("User not found."));

        Path storedFile = storageDirectory.resolve(UUID.randomUUID().toString()).normalize();
        if (!storedFile.startsWith(storageDirectory)) {
            throw new IllegalStateException("Invalid evidence storage path.");
        }
        try {
            Files.createDirectories(storageDirectory);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = new DigestInputStream(file.getInputStream(), digest)) {
                Files.copy(input, storedFile);
            }
            long storedSize = Files.size(storedFile);
            if (storedSize <= 0 || storedSize > Evidence.MAX_FILE_SIZE_BYTES) {
                Files.deleteIfExists(storedFile);
                throw new IllegalArgumentException("File size must be between 1 byte and 200 MB.");
            }

            Evidence evidence = new Evidence();
            evidence.setInvestigationCase(investigationCase);
            evidence.setUploadedBy(uploader);
            evidence.setFileName(safeFileName);
            evidence.setFormat(format);
            evidence.setFormatVersion(formatVersion);
            evidence.setFileSizeBytes(storedSize);
            evidence.setStoragePath(storedFile.toString());
            evidence.setSha256Hash(HexFormat.of().formatHex(digest.digest()));
            evidence.setIntegrityStatus(IntegrityStatus.PENDING);
            try {
                return evidenceRepository.save(evidence);
            } catch (RuntimeException e) {
                Files.deleteIfExists(storedFile);
                throw e;
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to store uploaded evidence.", e);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
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
