package com.tracevault.backend.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.EmailMessage;
import com.tracevault.backend.enms.EmailSignatureResult;
import com.tracevault.backend.enms.SpfResult;
import com.tracevault.backend.repository.EmailMessageRepository;
import com.tracevault.backend.repository.EvidenceRepository;

@Service
public class EmailMessageService {
    private final EmailMessageRepository emailMessageRepository;
    private final EvidenceRepository evidenceRepository;

    public EmailMessageService(EmailMessageRepository emailMessageRepository,
                               EvidenceRepository evidenceRepository) {
        this.emailMessageRepository = emailMessageRepository;
        this.evidenceRepository = evidenceRepository;
    }

    @Transactional
    public EmailMessage create(UUID evidenceId, EmailMessageInput input) {
        var evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found."));
        if (evidence.getFormat() != com.tracevault.backend.enms.EvidenceFormat.EML) {
            throw new IllegalArgumentException("Email header data can only be attached to EML evidence.");
        }
        if (emailMessageRepository.findByEvidence_EvidenceId(evidenceId).isPresent()) {
            throw new IllegalArgumentException("Email header data already exists for this evidence.");
        }

        EmailMessage email = new EmailMessage();
        email.setEvidence(evidence);
        apply(email, input);
        return emailMessageRepository.save(email);
    }

    @Transactional(readOnly = true)
    public EmailMessage getByEvidence(UUID evidenceId) {
        return emailMessageRepository.findByEvidence_EvidenceId(evidenceId)
                .orElseThrow(() -> new RuntimeException("Email header data not found."));
    }

    @Transactional
    public EmailMessage update(UUID evidenceId, EmailMessageInput input) {
        EmailMessage email = getByEvidence(evidenceId);
        apply(email, input);
        return emailMessageRepository.save(email);
    }

    private void apply(EmailMessage email, EmailMessageInput input) {
        email.setFromDisplayName(input.fromDisplayName());
        email.setFromAddress(input.fromAddress());
        email.setReplyTo(input.replyTo());
        email.setSubject(input.subject());
        email.setReceivedHops(input.receivedHops());
        email.setSpfResult(input.spfResult() == null ? SpfResult.NONE : input.spfResult());
        email.setDkimResult(input.dkimResult() == null ? EmailSignatureResult.NONE : input.dkimResult());
        email.setDmarcResult(input.dmarcResult() == null ? EmailSignatureResult.NONE : input.dmarcResult());
        email.setSentAt(input.sentAt());
    }

    public record EmailMessageInput(
            String fromDisplayName,
            String fromAddress,
            String replyTo,
            String subject,
            String receivedHops,
            SpfResult spfResult,
            EmailSignatureResult dkimResult,
            EmailSignatureResult dmarcResult,
            OffsetDateTime sentAt) {}
}
