package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.SystemPlaylist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SystemPlaylistRepoPort {

    SystemPlaylist save(SystemPlaylist playlist);

    Optional<SystemPlaylist> findById(UUID id);

    List<SystemPlaylist> findActive(String query, int page, int size);

    long countActive(String query);

    List<SystemPlaylist> findAll(String query, Boolean isActive, int page, int size);

    long countAll(String query, Boolean isActive);

    void deleteById(UUID id);
}
