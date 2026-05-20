package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.SystemPlaylistModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SystemPlaylistJpaRepo extends JpaRepository<SystemPlaylistModel, UUID> {

    Page<SystemPlaylistModel> findByIsActiveTrueAndNameContainingIgnoreCase(String query, Pageable pageable);

    long countByIsActiveTrueAndNameContainingIgnoreCase(String query);

    Page<SystemPlaylistModel> findByIsActiveTrue(Pageable pageable);

    long countByIsActiveTrue();

    Page<SystemPlaylistModel> findByNameContainingIgnoreCase(String query, Pageable pageable);

    long countByNameContainingIgnoreCase(String query);

    Page<SystemPlaylistModel> findByIsActive(boolean isActive, Pageable pageable);

    long countByIsActive(boolean isActive);

    Page<SystemPlaylistModel> findByIsActiveAndNameContainingIgnoreCase(boolean isActive, String query, Pageable pageable);

    long countByIsActiveAndNameContainingIgnoreCase(boolean isActive, String query);
}
