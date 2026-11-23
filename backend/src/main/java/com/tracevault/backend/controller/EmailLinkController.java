package com.tracevault.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.EmailLink;
import com.tracevault.backend.service.EmailLinkService;
import com.tracevault.backend.service.EmailLinkService.EmailLinkInput;

@RestController
@RequestMapping("/api/email-messages/{emailId}/links")
public class EmailLinkController {
    private final EmailLinkService emailLinkService;

    public EmailLinkController(EmailLinkService emailLinkService) {
        this.emailLinkService = emailLinkService;
    }

    @PostMapping
    public EmailLink create(@PathVariable UUID emailId, @RequestBody EmailLinkInput input) {
        return emailLinkService.create(emailId, input);
    }

    @GetMapping
    public List<EmailLink> getLinks(@PathVariable UUID emailId) {
        return emailLinkService.getLinks(emailId);
    }

    @GetMapping("/{linkId}")
    public EmailLink getLink(@PathVariable UUID emailId, @PathVariable UUID linkId) {
        return emailLinkService.getLink(emailId, linkId);
    }

    @PutMapping("/{linkId}")
    public EmailLink update(@PathVariable UUID emailId, @PathVariable UUID linkId,
                            @RequestBody EmailLinkInput input) {
        return emailLinkService.update(emailId, linkId, input);
    }

    @DeleteMapping("/{linkId}")
    public void delete(@PathVariable UUID emailId, @PathVariable UUID linkId) {
        emailLinkService.delete(emailId, linkId);
    }
}
