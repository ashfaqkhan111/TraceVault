package com.tracevault.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tracevault.backend.entity.Evidence;
import com.tracevault.backend.entity.MetadataField;
import com.tracevault.backend.repository.EvidenceRepository;
import com.tracevault.backend.repository.MetadataFieldRepository;

@Service
public class MetadataFieldService {
    private final MetadataFieldRepository metadataFieldRepository;
    private final EvidenceRepository evidenceRepository;

    public MetadataFieldService(MetadataFieldRepository metadataFieldRepository,
                                EvidenceRepository evidenceRepository) {
        this.metadataFieldRepository = metadataFieldRepository;
        this.evidenceRepository = evidenceRepository;
    }

    @Transactional
    public MetadataField addField(UUID evidenceId, String fieldKey, String fieldValue, boolean anomaly) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found."));

        MetadataField field = new MetadataField();
        field.setEvidence(evidence);
        field.setFieldKey(fieldKey);
        field.setFieldValue(fieldValue);
        field.setAnomaly(anomaly);
        return metadataFieldRepository.save(field);
    }

    @Transactional
    public List<MetadataField> addFields(UUID evidenceId, List<MetadataFieldInput> fields) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found."));

        List<MetadataField> metadataFields = fields.stream().map(input -> {
            MetadataField field = new MetadataField();
            field.setEvidence(evidence);
            field.setFieldKey(input.fieldKey());
            field.setFieldValue(input.fieldValue());
            field.setAnomaly(input.anomaly());
            return field;
        }).toList();

        return metadataFieldRepository.saveAll(metadataFields);
    }

    @Transactional(readOnly = true)
    public List<MetadataField> getFields(UUID evidenceId) {
        return metadataFieldRepository.findByEvidence_EvidenceIdOrderByFieldKeyAsc(evidenceId);
    }

    @Transactional(readOnly = true)
    public List<MetadataField> getAnomalies(UUID evidenceId) {
        return metadataFieldRepository.findByEvidence_EvidenceIdAndAnomalyTrueOrderByFieldKeyAsc(evidenceId);
    }

    @Transactional
    public MetadataField updateAnomaly(UUID evidenceId, UUID fieldId, boolean anomaly) {
        MetadataField field = findFieldForEvidence(evidenceId, fieldId);
        field.setAnomaly(anomaly);
        return metadataFieldRepository.save(field);
    }

    public MetadataSummary getSummary(UUID evidenceId) {
        return new MetadataSummary(
                metadataFieldRepository.countByEvidence_EvidenceId(evidenceId),
                metadataFieldRepository.countByEvidence_EvidenceIdAndAnomalyTrue(evidenceId));
    }

    public void deleteField(UUID evidenceId, UUID fieldId) {
        metadataFieldRepository.delete(findFieldForEvidence(evidenceId, fieldId));
    }

    private MetadataField findFieldForEvidence(UUID evidenceId, UUID fieldId) {
        MetadataField field = metadataFieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Metadata field not found."));
        if (!field.getEvidence().getEvidenceId().equals(evidenceId)) {
            throw new RuntimeException("Metadata field does not belong to this evidence.");
        }
        return field;
    }

    public record MetadataFieldInput(String fieldKey, String fieldValue, boolean anomaly) {}
    public record MetadataSummary(long fieldCount, long anomalyCount) {}
}
