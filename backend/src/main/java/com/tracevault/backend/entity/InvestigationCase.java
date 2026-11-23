package com.tracevault.backend.entity;

import java.util.UUID;

import com.tracevault.backend.enms.CaseStatus;

import java.time.OffsetDateTime;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity 
@Table (name = "investigation_case")
@Getter   
@Setter 
@NoArgsConstructor 
public class InvestigationCase {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    @Column (name = "case_id", nullable = false, updatable = false)
    private UUID caseId;

    @Column (name = "case_number", nullable = false, unique = true, length = 255)
    private String caseNumber;

    @Column (name = "title", nullable = false, length = 255)
    private String title;

    @Column (name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated (EnumType.STRING)
    @Column (name = "status", nullable = false, length = 30)
    private CaseStatus status = CaseStatus.OPEN;

    @Column (name = "open_at")
    private OffsetDateTime openAt;

    @Column (name = "closed_at")
    private OffsetDateTime closedAt;

    @Column (name = "update_at")
    private OffsetDateTime updateAt;

    @PrePersist 
    public void onCreate(){
        if(openAt == null){
            openAt = OffsetDateTime.now();
        }
    }

    @PreUpdate 
    protected void onUpdate(){
        updateAt = OffsetDateTime.now();
    }


}
