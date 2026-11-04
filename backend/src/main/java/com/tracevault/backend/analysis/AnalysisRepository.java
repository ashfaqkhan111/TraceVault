package com.tracevault.backend.analysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {

    /** A single user's history; the most recent entry is at the top. */
    List<Analysis> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

        /** Always search by ID and owner, so that User A cannot view User B's analytics. */
    Optional<Analysis> findByIdAndOwnerId(UUID id, UUID ownerId);
}
