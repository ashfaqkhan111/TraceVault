package com.tracevault.backend.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.tracevault.backend.enms.EvidenceFormat;
import com.tracevault.backend.enms.IntegrityStatus;

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
@Table(name = "evidence")
@Getter
@Setter
@NoArgsConstructor
public class Evidence {

    public static final long MAX_FILE_SIZE_BYTES = 200L * 1024 * 1024;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "evidence_id", nullable = false, updatable = false)
    private UUID evidenceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private InvestigationCase investigationCase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 10)
    private EvidenceFormat format;

    @Column(name = "format_version", length = 100)
    private String formatVersion;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes;

    @Column(name = "storage_path", nullable = false, length = 2048, updatable = false)
    private String storagePath;

    @Column(name = "sha256_hash", nullable = false, length = 64, updatable = false)
    private String sha256Hash;

    @Enumerated(EnumType.STRING)
    @Column(name = "integrity_status", nullable = false, length = 30)
    private IntegrityStatus integrityStatus = IntegrityStatus.PENDING;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private OffsetDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = OffsetDateTime.now();
        }
    }
}
