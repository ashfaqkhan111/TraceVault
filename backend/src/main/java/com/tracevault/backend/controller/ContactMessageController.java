package com.tracevault.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.ContactMessage;
import com.tracevault.backend.enms.ContactMessageStatus;
import com.tracevault.backend.service.ContactMessageService;
import com.tracevault.backend.service.ContactMessageService.ContactMessageInput;

@RestController
@RequestMapping("/api/contact-messages")
public class ContactMessageController {
    private final ContactMessageService contactMessageService;

    public ContactMessageController(ContactMessageService contactMessageService) {
        this.contactMessageService = contactMessageService;
    }

    @PostMapping
    public ContactMessage submit(@RequestBody ContactMessageInput input) {
        return contactMessageService.submit(input);
    }

    @GetMapping
    public List<ContactMessage> getAll() {
        return contactMessageService.getAll();
    }

    @GetMapping("/status/{status}")
    public List<ContactMessage> getByStatus(@PathVariable ContactMessageStatus status) {
        return contactMessageService.getByStatus(status);
    }

    @GetMapping("/{messageId}")
    public ContactMessage getById(@PathVariable UUID messageId) {
        return contactMessageService.getById(messageId);
    }

    @PutMapping("/{messageId}/status")
    public ContactMessage updateStatus(@PathVariable UUID messageId,
                                       @RequestParam ContactMessageStatus status) {
        return contactMessageService.updateStatus(messageId, status);
    }
}
