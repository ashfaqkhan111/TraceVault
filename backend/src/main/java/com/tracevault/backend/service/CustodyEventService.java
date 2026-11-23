package com.tracevault.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.CustodyEvent;
import com.tracevault.backend.enms.CustodyActorType;
import com.tracevault.backend.repository.CustodyEventRepository;
import com.tracevault.backend.repository.EvidenceRepository;
import com.tracevault.backend.repository.UserRepository;

@Service
public class CustodyEventService {
    private final CustodyEventRepository custodyEventRepository;
    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;

    public CustodyEventService(CustodyEventRepository custodyEventRepository,
                               EvidenceRepository evidenceRepository,
                               UserRepository userRepository) {
        this.custodyEventRepository = custodyEventRepository;
        this.evidenceRepository = evidenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CustodyEvent appendUserEvent(UUID evidenceId, String authenticatedEmail, CustodyEventInput input) {
        var evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found."));
        if (authenticatedEmail == null || authenticatedEmail.isBlank()
                || "anonymousUser".equals(authenticatedEmail)) {
            throw new IllegalArgumentException("An authenticated user is required to add a custody event.");
        }
        CustodyEvent event = new CustodyEvent();
        event.setEvidence(evidence);
        event.setActorType(CustodyActorType.USER);
        event.setActorUser(userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user was not found.")));
        event.setAction(input.action());
        event.setHashAtEvent(calculateCurrentHash(evidence.getStoragePath()));
        return custodyEventRepository.save(event);
    }

    private String calculateCurrentHash(String storagePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = new DigestInputStream(Files.newInputStream(Path.of(storagePath)), digest)) {
                input.transferTo(java.io.OutputStream.nullOutputStream());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (IOException e) {
            throw new RuntimeException("Unable to read evidence bytes for custody hash.", e);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<CustodyEvent> getTimeline(UUID evidenceId) {
        return custodyEventRepository.findByEvidence_EvidenceIdOrderByOccurredAtAsc(evidenceId);
    }

    public record CustodyEventInput(String action) {}
}
