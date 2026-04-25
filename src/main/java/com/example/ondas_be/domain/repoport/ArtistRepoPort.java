package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.Artist;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArtistRepoPort {

    Artist save(Artist artist);

    Optional<Artist> findById(UUID id);

    List<Artist> findAll(int page, int size);

    long countAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);

    boolean existsBySlug(String slug);

    List<Artist> findByIds(Collection<UUID> ids);

    List<Artist> findByNameContains(String query, int page, int size);

    long countByNameContains(String query);

    List<Artist> findByNameFullText(String query, int page, int size);

    long countByNameFullText(String query);
}
