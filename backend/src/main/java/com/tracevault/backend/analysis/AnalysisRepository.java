package com.tracevault.backend.analysis;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnalysisRepository extends JpaRepository<Analysis, UUID> {

    /** Riwayat milik satu user, terbaru di atas. */
    List<Analysis> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    /** Selalu cari berdasarkan id + pemilik, supaya user A tidak bisa membuka analisis user B. */
    Optional<Analysis> findByIdAndOwnerId(UUID id, UUID ownerId);
}
