package com.tracevault.backend.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tracevault.backend.enms.TimelineEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "timeline_event")
@Getter
@Setter
@NoArgsConstructor
public class TimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "timeline_event_id", nullable = false, updatable = false)
    private UUID timelineEventId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false, updatable = false)
    private InvestigationCase investigationCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_id", updatable = false)
    private Evidence evidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20, updatable = false)
    private TimelineEventType eventType;

    @Column(name = "title", nullable = false, length = 255, updatable = false)
    private String title;

    @Column(name = "detail", columnDefinition = "TEXT", updatable = false)
    private String detail;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "ai_group", length = 255, updatable = false)
    private String aiGroup;

    @PrePersist
    protected void onCreate() {
        if (occurredAt == null) {
            occurredAt = OffsetDateTime.now();
        }
    }
}
