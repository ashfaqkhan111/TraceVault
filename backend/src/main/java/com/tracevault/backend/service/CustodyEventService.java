package com.tracevault.backend.service;

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
    public CustodyEvent append(UUID evidenceId, CustodyEventInput input) {
        var evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found."));

        if (input.actorType() == CustodyActorType.USER && input.actorUserId() == null) {
            throw new IllegalArgumentException("User actor requires an actor user ID.");
        }
        if (input.actorType() != CustodyActorType.USER && input.actorUserId() != null) {
            throw new IllegalArgumentException("System and AI actors must not have an actor user ID.");
        }
        if (input.hashAtEvent() != null && !input.hashAtEvent().matches("(?i)[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Event hash must contain exactly 64 hexadecimal characters.");
        }

        CustodyEvent event = new CustodyEvent();
        event.setEvidence(evidence);
        event.setActorType(input.actorType());
        if (input.actorUserId() != null) {
            event.setActorUser(userRepository.findById(input.actorUserId())
                    .orElseThrow(() -> new RuntimeException("Actor user not found.")));
        }
        event.setAction(input.action());
        event.setHashAtEvent(input.hashAtEvent() == null ? null : input.hashAtEvent().toLowerCase());
        return custodyEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<CustodyEvent> getTimeline(UUID evidenceId) {
        return custodyEventRepository.findByEvidence_EvidenceIdOrderByOccurredAtAsc(evidenceId);
    }

    public record CustodyEventInput(CustodyActorType actorType, UUID actorUserId,
                                    String action, String hashAtEvent) {}
}
