package com.tracevault.backend.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import com.tracevault.backend.enms.AnalysisModule;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class AnalysisModuleScoreId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "analysis_id", nullable = false)
    private UUID analysisId;

    @Enumerated(EnumType.STRING)
    @Column(name = "module", nullable = false, length = 30)
    private AnalysisModule module;

    public AnalysisModuleScoreId(UUID analysisId, AnalysisModule module) {
        this.analysisId = analysisId;
        this.module = module;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof AnalysisModuleScoreId that)) return false;
        return Objects.equals(analysisId, that.analysisId) && module == that.module;
    }

    @Override
    public int hashCode() {
        return Objects.hash(analysisId, module);
    }
}
