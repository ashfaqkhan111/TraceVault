package com.tracevault.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.EmailLink;
import com.tracevault.backend.enms.EmailLinkVerdict;

public interface EmailLinkRepository extends JpaRepository<EmailLink, UUID> {
    List<EmailLink> findByEmailMessage_EmailIdOrderByLinkIdAsc(UUID emailId);

    List<EmailLink> findByEmailMessage_EmailIdAndVerdictOrderByLinkIdAsc(
            UUID emailId, EmailLinkVerdict verdict);
}
