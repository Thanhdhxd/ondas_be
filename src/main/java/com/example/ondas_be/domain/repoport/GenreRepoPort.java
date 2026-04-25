package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GenreRepoPort {

    Genre save(Genre genre);

    Optional<Genre> findById(Long id);

    List<Genre> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsBySlug(String slug);

    List<Genre> findByIds(Collection<Long> ids);

    List<Genre> findByNameContains(String query, int page, int size);

    long countByNameContains(String query);

    List<Genre> findByNameFullText(String query, int page, int size);

    long countByNameFullText(String query);
}
