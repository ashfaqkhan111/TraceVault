package com.tracevault.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracevault.backend.entity.CustodyEvent;

public interface CustodyEventRepository extends JpaRepository<CustodyEvent, UUID> {
    List<CustodyEvent> findByEvidence_EvidenceIdOrderByOccurredAtAsc(UUID evidenceId);
}
