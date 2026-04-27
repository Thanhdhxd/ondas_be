package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.Playlist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaylistRepoPort {

    Playlist save(Playlist playlist);

    Optional<Playlist> findById(UUID id);

    List<Playlist> findPublic(String query, int page, int size);

    long countPublic(String query);

    List<Playlist> findByUserId(UUID userId, Boolean isPublic, String query, int page, int size);

    long countByUserId(UUID userId, Boolean isPublic, String query);

    void deleteById(UUID id);
}
