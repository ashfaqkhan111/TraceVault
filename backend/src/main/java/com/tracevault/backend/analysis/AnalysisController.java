package com.tracevault.backend.analysis;

import com.tracevault.backend.auth.AuthDtos.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Semua endpoint di sini butuh login (header Authorization: Bearer <token>).
 * userId diambil dari token, BUKAN dari data yang dikirim user.
 *
 * POST   /api/analyses              → upload file (form-data: file, type, notes)
 * GET    /api/analyses              → riwayat analisis milik user
 * GET    /api/analyses/{id}         → detail satu analisis
 * DELETE /api/analyses/{id}         → hapus analisis
 * GET    /api/analyses/{id}/report  → laporan untuk diunduh
 */
@RestController
@RequestMapping("/api/analyses")
public class AnalysisController {

    private final AnalysisService analysisService;
    private final ReportService reportService;

    public AnalysisController(AnalysisService analysisService, ReportService reportService) {
        this.analysisService = analysisService;
        this.reportService = reportService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResponse submit(@AuthenticationPrincipal UUID userId,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "type", defaultValue = "file") String type,
                                   @RequestParam(value = "notes", required = false) String notes) {
        return analysisService.submit(userId, file, type, notes);
    }

    @GetMapping
    public List<AnalysisResponse> list(@AuthenticationPrincipal UUID userId) {
        return analysisService.list(userId);
    }

    @GetMapping("/{id}")
    public AnalysisResponse get(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        return analysisService.get(userId, id);
    }

    @DeleteMapping("/{id}")
    public MessageResponse delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        analysisService.delete(userId, id);
        return new MessageResponse("Deleted.");
    }

    @GetMapping("/{id}/report")
    public ReportService.ReportResponse report(@AuthenticationPrincipal UUID userId, @PathVariable UUID id,
                                               @RequestParam(value = "format", defaultValue = "text") String format) {
        return reportService.build(analysisService.get(userId, id));
    }
}
