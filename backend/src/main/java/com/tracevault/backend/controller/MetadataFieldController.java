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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracevault.backend.entity.MetadataField;
import com.tracevault.backend.service.MetadataFieldService;
import com.tracevault.backend.service.MetadataFieldService.MetadataFieldInput;
import com.tracevault.backend.service.MetadataFieldService.MetadataSummary;

@RestController
@RequestMapping("/api/evidence/{evidenceId}/metadata")
public class MetadataFieldController {
    private final MetadataFieldService metadataFieldService;

    public MetadataFieldController(MetadataFieldService metadataFieldService) {
        this.metadataFieldService = metadataFieldService;
    }

    @PostMapping
    public MetadataField addField(@PathVariable UUID evidenceId,
                                  @RequestParam String fieldKey,
                                  @RequestParam(required = false) String fieldValue,
                                  @RequestParam(defaultValue = "false") boolean anomaly) {
        return metadataFieldService.addField(evidenceId, fieldKey, fieldValue, anomaly);
    }

    @PostMapping("/batch")
    public List<MetadataField> addFields(@PathVariable UUID evidenceId,
                                         @RequestBody List<MetadataFieldInput> fields) {
        return metadataFieldService.addFields(evidenceId, fields);
    }

    @GetMapping
    public List<MetadataField> getFields(@PathVariable UUID evidenceId) {
        return metadataFieldService.getFields(evidenceId);
    }

    @GetMapping("/anomalies")
    public List<MetadataField> getAnomalies(@PathVariable UUID evidenceId) {
        return metadataFieldService.getAnomalies(evidenceId);
    }

    @GetMapping("/summary")
    public MetadataSummary getSummary(@PathVariable UUID evidenceId) {
        return metadataFieldService.getSummary(evidenceId);
    }

    @PutMapping("/{fieldId}/anomaly")
    public MetadataField updateAnomaly(@PathVariable UUID evidenceId,
                                       @PathVariable UUID fieldId,
                                       @RequestParam boolean anomaly) {
        return metadataFieldService.updateAnomaly(evidenceId, fieldId, anomaly);
    }

    @DeleteMapping("/{fieldId}")
    public void deleteField(@PathVariable UUID evidenceId, @PathVariable UUID fieldId) {
        metadataFieldService.deleteField(evidenceId, fieldId);
    }
}
