package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.Album;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AlbumRepoPort {

    Album save(Album album);

    Optional<Album> findById(UUID id);

    List<Album> findAll(int page, int size);

    long countAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsBySlug(String slug);

    List<Album> findByTitleContains(String query, int page, int size);

    long countByTitleContains(String query);

    List<Album> findByTitleFullText(String query, int page, int size);

    long countByTitleFullText(String query);
}
