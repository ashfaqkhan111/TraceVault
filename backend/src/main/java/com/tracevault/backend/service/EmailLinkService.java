package com.tracevault.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.EmailLink;
import com.tracevault.backend.enms.EmailLinkVerdict;
import com.tracevault.backend.repository.EmailLinkRepository;
import com.tracevault.backend.repository.EmailMessageRepository;

@Service
public class EmailLinkService {
    private final EmailLinkRepository emailLinkRepository;
    private final EmailMessageRepository emailMessageRepository;

    public EmailLinkService(EmailLinkRepository emailLinkRepository,
                            EmailMessageRepository emailMessageRepository) {
        this.emailLinkRepository = emailLinkRepository;
        this.emailMessageRepository = emailMessageRepository;
    }

    @Transactional
    public EmailLink create(UUID emailId, EmailLinkInput input) {
        var emailMessage = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new RuntimeException("Email message not found."));
        EmailLink link = new EmailLink();
        link.setEmailMessage(emailMessage);
        link.setUrl(input.url());
        link.setResolvedDomain(input.resolvedDomain());
        link.setVerdict(input.verdict());
        return emailLinkRepository.save(link);
    }

    @Transactional(readOnly = true)
    public List<EmailLink> getLinks(UUID emailId) {
        return emailLinkRepository.findByEmailMessage_EmailIdOrderByLinkIdAsc(emailId);
    }

    @Transactional(readOnly = true)
    public EmailLink getLink(UUID emailId, UUID linkId) {
        EmailLink link = emailLinkRepository.findById(linkId)
                .orElseThrow(() -> new RuntimeException("Email link not found."));
        if (!link.getEmailMessage().getEmailId().equals(emailId)) {
            throw new RuntimeException("Email link does not belong to this email message.");
        }
        return link;
    }

    @Transactional
    public EmailLink update(UUID emailId, UUID linkId, EmailLinkInput input) {
        EmailLink link = getLink(emailId, linkId);
        link.setUrl(input.url());
        link.setResolvedDomain(input.resolvedDomain());
        link.setVerdict(input.verdict());
        return emailLinkRepository.save(link);
    }

    @Transactional
    public void delete(UUID emailId, UUID linkId) {
        emailLinkRepository.delete(getLink(emailId, linkId));
    }

    public record EmailLinkInput(String url, String resolvedDomain, EmailLinkVerdict verdict) {}
}
