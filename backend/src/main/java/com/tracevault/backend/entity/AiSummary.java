package com.tracevault.backend.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ai_summary")
@Getter
@Setter
@NoArgsConstructor
public class AiSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "summary_id", nullable = false, updatable = false)
    private UUID summaryId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false, unique = true)
    private Analysis analysis;

    @Column(name = "summary_text", nullable = false, columnDefinition = "TEXT")
    private String summaryText;

    @ElementCollection
    @CollectionTable(name = "ai_summary_key_risk", joinColumns = @JoinColumn(name = "summary_id"))
    @Column(name = "key_risk", nullable = false, columnDefinition = "TEXT")
    private List<String> keyRisks = new ArrayList<>();

    @Column(name = "confidence_pct", nullable = false)
    private int confidencePct;

    @Column(name = "flow_id", nullable = false, length = 255)
    private String flowId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private OffsetDateTime generatedAt;

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = OffsetDateTime.now();
        }
    }
}
