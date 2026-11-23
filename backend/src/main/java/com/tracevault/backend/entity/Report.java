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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "report")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "report_id", nullable = false, updatable = false)
    private UUID reportId;

    @Column(name = "report_code", nullable = false, unique = true, length = 50)
    private String reportCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;

    @ElementCollection
    @CollectionTable(name = "report_limitation", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "limitation", nullable = false, columnDefinition = "TEXT")
    private List<String> limitations = new ArrayList<>();

    @Column(name = "pdf_path", nullable = false, length = 2048)
    private String pdfPath;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private OffsetDateTime generatedAt;

    @Column(name = "emailed_to", length = 320)
    private String emailedTo;

    @Column(name = "emailed_at")
    private OffsetDateTime emailedAt;

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = OffsetDateTime.now();
        }
    }
}
