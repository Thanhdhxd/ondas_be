package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.LyricsModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LyricsJpaRepo extends JpaRepository<LyricsModel, UUID> {
    Optional<LyricsModel> findBySongId(UUID songId);
    void deleteBySongId(UUID songId);
}
