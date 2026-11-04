package com.tracevault.backend.analysis;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The “analyses” table: one row = one file that has been analyzed.
 * Lists of findings, metadata, recommendations, and timelines are stored in their respective child tables.
 * The original files are NOT stored (only hashes and analysis results).
 */
@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "analysis_type", nullable = false)
    private String type;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String sha256;

    @Column(nullable = false)
    private String md5;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @Column(name = "ai_summary")
    private String aiSummary;

    private String notes;

    @Column(name = "duration_ms", nullable = false)
    private long durationMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @ElementCollection
    @CollectionTable(name = "analysis_findings", joinColumns = @JoinColumn(name = "analysis_id"))
    @OrderColumn(name = "position")
    private List<FindingItem> findings = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "analysis_metadata", joinColumns = @JoinColumn(name = "analysis_id"))
    @OrderColumn(name = "position")
    private List<MetadataItem> metadata = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "analysis_recommendations", joinColumns = @JoinColumn(name = "analysis_id"))
    @OrderColumn(name = "position")
    @Column(name = "recommendation", nullable = false)
    private List<String> recommendations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "analysis_timeline", joinColumns = @JoinColumn(name = "analysis_id"))
    @OrderColumn(name = "position")
    private List<TimelineItem> timeline = new ArrayList<>();

    protected Analysis() {}  // JPA is required

    public Analysis(UUID ownerId, String fileName, long fileSize, String type, String notes, Instant createdAt) {
        this.ownerId = ownerId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.type = type;
        this.notes = notes;
        this.createdAt = createdAt;
        this.status = "processing";
    }

    public void complete(String sha256, String md5, int riskScore, String riskLevel, String aiSummary,
                         Instant completedAt) {
        this.sha256 = sha256;
        this.md5 = md5;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.aiSummary = aiSummary;
        this.completedAt = completedAt;
        this.durationMs = completedAt.toEpochMilli() - createdAt.toEpochMilli();
        this.status = "completed";
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getFileName() { return fileName; }
    public long getFileSize() { return fileSize; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getSha256() { return sha256; }
    public String getMd5() { return md5; }
    public int getRiskScore() { return riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public String getAiSummary() { return aiSummary; }
    public String getNotes() { return notes; }
    public long getDurationMs() { return durationMs; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public List<FindingItem> getFindings() { return findings; }
    public List<MetadataItem> getMetadata() { return metadata; }
    public List<String> getRecommendations() { return recommendations; }
    public List<TimelineItem> getTimeline() { return timeline; }

    // ---------------------------------------------------------- table

    @Embeddable
    public static class FindingItem {
        @Column(nullable = false) private String code;
        @Column(nullable = false) private String severity;
        @Column(nullable = false) private String title;
        @Column(nullable = false) private String description;
        @Column(nullable = false) private String category;

        protected FindingItem() {}

        public FindingItem(String code, String severity, String title, String description, String category) {
            this.code = code;
            this.severity = severity;
            this.title = title;
            this.description = description;
            this.category = category;
        }

        public String getCode() { return code; }
        public String getSeverity() { return severity; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
    }

    @Embeddable
    public static class MetadataItem {
        @Column(name = "meta_key", nullable = false) private String key;
        @Column(name = "meta_value", nullable = false) private String value;

        protected MetadataItem() {}

        public MetadataItem(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() { return key; }
        public String getValue() { return value; }
    }

    @Embeddable
    public static class TimelineItem {
        @Column(name = "event_time", nullable = false) private Instant time;
        @Column(nullable = false) private String title;
        @Column(nullable = false) private String description;

        protected TimelineItem() {}

        public TimelineItem(Instant time, String title, String description) {
            this.time = time;
            this.title = title;
            this.description = description;
        }

        public Instant getTime() { return time; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }
}
