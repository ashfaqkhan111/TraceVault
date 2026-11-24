package com.tracevault.backend.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.EmailMessage;
import com.tracevault.backend.service.EmailMessageService;
import com.tracevault.backend.service.EmailMessageService.EmailMessageInput;

@RestController
@RequestMapping("/api/evidence/{evidenceId}/email-message")
public class EmailMessageController {
    private final EmailMessageService emailMessageService;

    public EmailMessageController(EmailMessageService emailMessageService) {
        this.emailMessageService = emailMessageService;
    }

    @PostMapping
    public EmailMessage create(@PathVariable UUID evidenceId, @RequestBody EmailMessageInput input) {
        return emailMessageService.create(evidenceId, input);
    }

    @GetMapping
    public EmailMessage getByEvidence(@PathVariable UUID evidenceId) {
        return emailMessageService.getByEvidence(evidenceId);
    }

    @PutMapping
    public EmailMessage update(@PathVariable UUID evidenceId, @RequestBody EmailMessageInput input) {
        return emailMessageService.update(evidenceId, input);
    }
}
