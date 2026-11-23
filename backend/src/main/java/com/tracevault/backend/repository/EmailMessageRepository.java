package com.tracevault.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.EmailMessage;

public interface EmailMessageRepository extends JpaRepository<EmailMessage, UUID> {
    Optional<EmailMessage> findByEvidence_EvidenceId(UUID evidenceId);
}
