package com.tracevault.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.TimelineEvent;
import com.tracevault.backend.enms.TimelineEventType;
import com.tracevault.backend.repository.EvidenceRepository;
import com.tracevault.backend.repository.InvestigationCaseRepository;
import com.tracevault.backend.repository.TimelineEventRepository;

@Service
public class TimelineEventService {
    private final TimelineEventRepository timelineEventRepository;
    private final InvestigationCaseRepository caseRepository;
    private final EvidenceRepository evidenceRepository;

    public TimelineEventService(TimelineEventRepository timelineEventRepository,
                                InvestigationCaseRepository caseRepository,
                                EvidenceRepository evidenceRepository) {
        this.timelineEventRepository = timelineEventRepository;
        this.caseRepository = caseRepository;
        this.evidenceRepository = evidenceRepository;
    }

    @Transactional
    public TimelineEvent append(UUID caseId, TimelineEventInput input) {
        var investigationCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Investigation case not found."));

        TimelineEvent event = new TimelineEvent();
        event.setInvestigationCase(investigationCase);
        if (input.evidenceId() != null) {
            var evidence = evidenceRepository.findById(input.evidenceId())
                    .orElseThrow(() -> new RuntimeException("Evidence not found."));
            if (evidence.getInvestigationCase() == null
                    || !evidence.getInvestigationCase().getCaseId().equals(caseId)) {
                throw new IllegalArgumentException("Evidence does not belong to this case.");
            }
            event.setEvidence(evidence);
        }
        event.setEventType(input.eventType());
        event.setTitle(input.title());
        event.setDetail(input.detail());
        event.setAiGroup(input.aiGroup());
        if (input.occurredAt() != null) {
            event.setOccurredAt(input.occurredAt());
        }
        return timelineEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<TimelineEvent> getForCase(UUID caseId) {
        return timelineEventRepository
                .findByInvestigationCase_CaseIdOrderByOccurredAtAscTimelineEventIdAsc(caseId);
    }

    public record TimelineEventInput(UUID evidenceId, TimelineEventType eventType,
                                     String title, String detail, OffsetDateTime occurredAt,
                                     String aiGroup) {}
}
