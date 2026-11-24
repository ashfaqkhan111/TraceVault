package com.tracevault.backend.entity;

import com.tracevault.backend.enms.AnalysisModule;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "module_score")
@Getter
@Setter
@NoArgsConstructor
public class ModuleScore {

    @EmbeddedId
    private AnalysisModuleScoreId id;

    @MapsId("analysisId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;

    @Column(name = "score", nullable = false)
    private int score;

    public ModuleScore(Analysis analysis, AnalysisModule module, int score) {
        this.analysis = analysis;
        this.id = new AnalysisModuleScoreId(analysis.getAnalysisId(), module);
        this.score = score;
    }

    @Transient
    public AnalysisModule getModule() {
        return id == null ? null : id.getModule();
    }
}
