package com.tracevault.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.ContactMessage;
import com.tracevault.backend.enms.ContactMessageStatus;
import com.tracevault.backend.repository.ContactMessageRepository;
import com.tracevault.backend.repository.UserRepository;

@Service
public class ContactMessageService {
    private final ContactMessageRepository contactMessageRepository;
    private final UserRepository userRepository;

    public ContactMessageService(ContactMessageRepository contactMessageRepository,
                                 UserRepository userRepository) {
        this.contactMessageRepository = contactMessageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ContactMessage submit(ContactMessageInput input) {
        if (!input.consent()) {
            throw new IllegalArgumentException("Consent is required to submit the contact form.");
        }
        ContactMessage contactMessage = new ContactMessage();
        if (input.userId() != null) {
            contactMessage.setUser(userRepository.findById(input.userId())
                    .orElseThrow(() -> new RuntimeException("User not found.")));
        }
        contactMessage.setFullName(input.fullName());
        contactMessage.setEmail(input.email());
        contactMessage.setTopic(input.topic());
        contactMessage.setMessage(input.message());
        contactMessage.setConsent(true);
        contactMessage.setStatus(ContactMessageStatus.OPEN);
        return contactMessageRepository.save(contactMessage);
    }

    @Transactional(readOnly = true)
    public List<ContactMessage> getAll() {
        return contactMessageRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<ContactMessage> getByStatus(ContactMessageStatus status) {
        return contactMessageRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public ContactMessage getById(UUID messageId) {
        return contactMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Contact message not found."));
    }

    @Transactional
    public ContactMessage updateStatus(UUID messageId, ContactMessageStatus status) {
        ContactMessage contactMessage = getById(messageId);
        contactMessage.setStatus(status);
        return contactMessageRepository.save(contactMessage);
    }

    public record ContactMessageInput(UUID userId, String fullName, String email,
                                      com.tracevault.backend.enms.ContactTopic topic,
                                      String message, boolean consent) {}
}
