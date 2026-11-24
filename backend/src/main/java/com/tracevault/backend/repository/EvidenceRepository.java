package com.tracevault.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.Evidence;
import com.tracevault.backend.enms.IntegrityStatus;

public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {
    List<Evidence> findByInvestigationCase_CaseIdOrderByUploadedAtDesc(UUID caseId);

    List<Evidence> findByUploadedBy_UserIdOrderByUploadedAtDesc(UUID userId);

    List<Evidence> findByInvestigationCase_CaseIdAndIntegrityStatusOrderByUploadedAtDesc(
            UUID caseId, IntegrityStatus integrityStatus);
}
