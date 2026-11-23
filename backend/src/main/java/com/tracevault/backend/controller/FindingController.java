package com.tracevault.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.Finding;
import com.tracevault.backend.service.FindingService;
import com.tracevault.backend.service.FindingService.FindingInput;

@RestController
@RequestMapping("/api/analyses/{analysisId}/findings")
public class FindingController {
    private final FindingService findingService;

    public FindingController(FindingService findingService) {
        this.findingService = findingService;
    }

    @PostMapping
    public Finding create(@PathVariable UUID analysisId, @RequestBody FindingInput input) {
        return findingService.create(analysisId, input);
    }

    @GetMapping
    public List<Finding> getForAnalysis(@PathVariable UUID analysisId) {
        return findingService.getForAnalysis(analysisId);
    }

    @GetMapping("/{findingId}")
    public Finding get(@PathVariable UUID analysisId, @PathVariable UUID findingId) {
        return findingService.get(analysisId, findingId);
    }

    @PutMapping("/{findingId}")
    public Finding update(@PathVariable UUID analysisId, @PathVariable UUID findingId,
                          @RequestBody FindingInput input) {
        return findingService.update(analysisId, findingId, input);
    }

    @DeleteMapping("/{findingId}")
    public void delete(@PathVariable UUID analysisId, @PathVariable UUID findingId) {
        findingService.delete(analysisId, findingId);
    }
}
