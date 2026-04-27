package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.PlaylistModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlaylistJpaRepo extends JpaRepository<PlaylistModel, UUID> {

    Page<PlaylistModel> findByIsPublicTrueAndNameContainingIgnoreCase(String query, Pageable pageable);

    long countByIsPublicTrueAndNameContainingIgnoreCase(String query);

    Page<PlaylistModel> findByIsPublicTrue(Pageable pageable);

    long countByIsPublicTrue();

    Page<PlaylistModel> findByUserIdAndNameContainingIgnoreCase(UUID userId, String query, Pageable pageable);

    long countByUserIdAndNameContainingIgnoreCase(UUID userId, String query);

    Page<PlaylistModel> findByUserId(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

    Page<PlaylistModel> findByUserIdAndIsPublic(UUID userId, boolean isPublic, Pageable pageable);

    long countByUserIdAndIsPublic(UUID userId, boolean isPublic);

    Page<PlaylistModel> findByUserIdAndIsPublicAndNameContainingIgnoreCase(UUID userId, boolean isPublic, String query,
            Pageable pageable);

    long countByUserIdAndIsPublicAndNameContainingIgnoreCase(UUID userId, boolean isPublic, String query);
}
