package com.tracevault.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.InvestigationCase;
import com.tracevault.backend.enms.CaseStatus;
import com.tracevault.backend.enms.CasePriority;

public interface InvestigationCaseRepository extends JpaRepository<InvestigationCase, UUID> {

    Optional<InvestigationCase> findByCaseNumber(String caseNumber);

    List<InvestigationCase> findByCreatedBy_UserId(UUID userid);

    List<InvestigationCase> findByStatus(CaseStatus status);

    List<InvestigationCase> findByCreatedBy_UserIdAndStatus(UUID userId, CaseStatus status);

    boolean existByCaseNumber(String caseNumber);

}
