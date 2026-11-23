package com.tracevault.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.TimelineEvent;

public interface TimelineEventRepository extends JpaRepository<TimelineEvent, UUID> {
    List<TimelineEvent> findByInvestigationCase_CaseIdOrderByOccurredAtAscTimelineEventIdAsc(UUID caseId);
}
