package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.Genre;
import com.example.ondas_be.domain.repoport.GenreRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.GenreJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.GenreModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreAdapter implements GenreRepoPort {

    private final GenreJpaRepo genreJpaRepo;

    @Override
    public Genre save(Genre genre) {
        return genreJpaRepo.save(GenreModel.fromDomain(genre)).toDomain();
    }

    @Override
    public Optional<Genre> findById(Long id) {
        return genreJpaRepo.findById(id).map(GenreModel::toDomain);
    }

    @Override
    public List<Genre> findAll() {
        return genreJpaRepo.findAll().stream().map(GenreModel::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        genreJpaRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return genreJpaRepo.existsById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return genreJpaRepo.existsBySlug(slug);
    }

    @Override
    public List<Genre> findByNameContains(String query, int page, int size) {
        return genreJpaRepo.findByNameContains(query, PageRequest.of(page, size))
                .map(GenreModel::toDomain)
                .toList();
    }

    @Override
    public long countByNameContains(String query) {
        return genreJpaRepo.countByNameContains(query);
    }

    @Override
    public List<Genre> findByNameFullText(String query, int page, int size) {
        return genreJpaRepo.findByNameFullText(query, PageRequest.of(page, size))
                .map(GenreModel::toDomain)
                .toList();
    }

    @Override
    public long countByNameFullText(String query) {
        return genreJpaRepo.countByNameFullText(query);
    }

    @Override
    public List<Genre> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return genreJpaRepo.findAllById(ids).stream()
                .map(GenreModel::toDomain).toList();
    }
}
