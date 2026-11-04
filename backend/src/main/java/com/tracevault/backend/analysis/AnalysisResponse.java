package com.tracevault.backend.analysis;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bentuk JSON hasil analisis yang dikirim ke frontend.
 * Nama field dibuat SAMA PERSIS dengan mockAnalysisResult() di frontend/js/api.js,
 * supaya halaman results/history/dashboard tidak perlu diubah.
 */
public record AnalysisResponse(
        String id,
        String name,
        long size,
        String type,
        String status,
        String createdAt,
        String completedAt,
        String duration,
        Hash hash,
        int riskScore,
        String riskLevel,
        List<FindingView> findings,
        Counts counts,
        Map<String, String> metadata,
        String aiSummary,
        List<String> recommendations,
        List<TimelineView> timeline,
        String notes) {

    public record Hash(String sha256, String md5) {}

    public record FindingView(String id, String severity, String title, String desc, String category) {}

    public record Counts(long critical, long warning, long info, long ok) {}

    public record TimelineView(String time, String title, String desc) {}

    public static AnalysisResponse of(Analysis a) {
        List<FindingView> findings = a.getFindings().stream()
                .map(f -> new FindingView(f.getCode(), f.getSeverity(), f.getTitle(), f.getDescription(), f.getCategory()))
                .toList();

        Map<String, String> metadata = new LinkedHashMap<>();
        a.getMetadata().forEach(m -> metadata.put(m.getKey(), m.getValue()));

        Counts counts = new Counts(
                findings.stream().filter(f -> f.severity().equals("critical")).count(),
                findings.stream().filter(f -> f.severity().equals("warning")).count(),
                findings.stream().filter(f -> f.severity().equals("info")).count(),
                findings.stream().filter(f -> f.severity().equals("ok")).count());

        long ms = a.getDurationMs();
        String duration = ms < 1000 ? ms + " ms" : String.format(java.util.Locale.ROOT, "%.1f s", ms / 1000.0);

        return new AnalysisResponse(
                a.getId().toString(),
                a.getFileName(),
                a.getFileSize(),
                a.getType(),
                a.getStatus(),
                a.getCreatedAt().toString(),
                a.getCompletedAt() == null ? null : a.getCompletedAt().toString(),
                duration,
                new Hash(a.getSha256(), a.getMd5()),
                a.getRiskScore(),
                a.getRiskLevel(),
                findings,
                counts,
                metadata,
                a.getAiSummary(),
                List.copyOf(a.getRecommendations()),
                a.getTimeline().stream()
                        .map(t -> new TimelineView(t.getTime().toString(), t.getTitle(), t.getDescription()))
                        .toList(),
                a.getNotes());
    }
}
