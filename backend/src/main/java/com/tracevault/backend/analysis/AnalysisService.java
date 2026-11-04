package com.tracevault.backend.analysis;

import com.tracevault.backend.analysis.Analysis.FindingItem;
import com.tracevault.backend.analysis.Analysis.MetadataItem;
import com.tracevault.backend.analysis.Analysis.TimelineItem;
import com.tracevault.backend.forensic.Finding;
import com.tracevault.backend.forensic.ForensicReport;
import com.tracevault.backend.forensic.ForensicService;
import com.tracevault.backend.forensic.SummaryWriter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Main workflow: receive file → check → save results → send back to the frontend. */
@Service
public class AnalysisService {

    private static final Set<String> TYPES = Set.of("file", "email", "malware");
    private static final long MAX_SIZE = 50L * 1024 * 1024; // 50 MB, sama dengan frontend

    private final AnalysisRepository analyses;
    private final ForensicService forensicService;

    public AnalysisService(AnalysisRepository analyses, ForensicService forensicService) {
        this.analyses = analyses;
        this.forensicService = forensicService;
    }

    @Transactional
    public AnalysisResponse submit(UUID ownerId, MultipartFile file, String type, String notes) {
        // 1. Validate input (don't rely solely on the browser's validation)
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please choose a file to analyze.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "File is larger than 50 MB.");
        }
        String safeType = type == null ? "file" : type.trim().toLowerCase();
        if (!TYPES.contains(safeType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Analysis type must be file, email, or malware.");
        }
        String fileName = cleanFileName(file.getOriginalFilename());
        String cleanNotes = notes == null || notes.isBlank() ? null : notes.trim().substring(0, Math.min(notes.trim().length(), 2000));

        byte[] data;
        try {
            data = file.getBytes(); // Read-only in memory; not saved to disk
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file could not be read.");
        }

        // 2. Forensic Examination
        Instant started = Instant.now();
        Analysis analysis = new Analysis(ownerId, fileName, data.length, safeType, cleanNotes, started);
        List<TimelineItem> timeline = analysis.getTimeline();
        timeline.add(new TimelineItem(started, "File uploaded", "Evidence file \"" + fileName + "\" was submitted for analysis."));

        ForensicReport report = forensicService.analyze(data, fileName, safeType);
        Instant hashed = Instant.now();
        timeline.add(new TimelineItem(hashed, "Hash computed", "SHA-256: " + report.getSha256().substring(0, 16) + "…"));
        timeline.add(new TimelineItem(hashed, "Metadata extracted", report.getMetadata().size() + " metadata fields extracted."));
        timeline.add(new TimelineItem(hashed, "Forensic checks completed",
                report.getFindings().size() + " findings identified (" + report.count("critical") + " critical, "
                        + report.count("warning") + " warnings)."));

        // 3. Score, summary, recommendations
        int score = SummaryWriter.riskScore(report);
        String summary = SummaryWriter.summary(report, safeType);
        Instant done = Instant.now();
        timeline.add(new TimelineItem(done, "Summary generated", "Summary produced from the recorded findings."));
        timeline.add(new TimelineItem(done, "Analysis complete", "Full report available for review and export."));

        int i = 1;
        for (Finding f : report.getFindings()) {
            analysis.getFindings().add(new FindingItem("f" + i++, f.severity(), cut(f.title(), 255), f.desc(), f.category()));
        }
        report.getMetadata().forEach((k, v) -> analysis.getMetadata().add(new MetadataItem(cut(k, 100), v)));
        analysis.getRecommendations().addAll(SummaryWriter.recommendations(report, safeType));
        analysis.complete(report.getSha256(), report.getMd5(), score, SummaryWriter.riskLevel(score), summary, done);

        // 4. Save to database
        return AnalysisResponse.of(analyses.save(analysis));
    }

    @Transactional(readOnly = true)
    public List<AnalysisResponse> list(UUID ownerId) {
        return analyses.findByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(AnalysisResponse::of).toList();
    }

    @Transactional(readOnly = true)
    public AnalysisResponse get(UUID ownerId, UUID id) {
        return AnalysisResponse.of(find(ownerId, id));
    }

    @Transactional
    public void delete(UUID ownerId, UUID id) {
        analyses.delete(find(ownerId, id));
    }

    @Transactional(readOnly = true)
    public Analysis find(UUID ownerId, UUID id) {
        return analyses.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found."));
    }

    /** Remove folder paths and unusual characters from the file name. */
    private static String cleanFileName(String original) {
        if (original == null || original.isBlank()) return "unnamed";
        String name = original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        name = name.replaceAll("[\\p{Cntrl}]", "").trim();
        return name.isEmpty() ? "unnamed" : cut(name, 255);
    }

    private static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
