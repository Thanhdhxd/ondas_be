package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.Artist;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.ArtistJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.ArtistModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ArtistAdapter implements ArtistRepoPort {

    private final ArtistJpaRepo artistJpaRepo;

    @Override
    public Artist save(Artist artist) {
        return artistJpaRepo.save(ArtistModel.fromDomain(artist)).toDomain();
    }

    @Override
    public Optional<Artist> findById(UUID id) {
        return artistJpaRepo.findById(id).map(ArtistModel::toDomain);
    }

    @Override
    public List<Artist> findAll(int page, int size) {
        return artistJpaRepo.findAll(PageRequest.of(page, size)).map(ArtistModel::toDomain).toList();
    }

    @Override
    public long countAll() {
        return artistJpaRepo.count();
    }

    @Override
    public void deleteById(UUID id) {
        artistJpaRepo.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return artistJpaRepo.existsById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return artistJpaRepo.existsBySlug(slug);
    }

    @Override
    public List<Artist> findByNameContains(String query, int page, int size) {
        return artistJpaRepo.findByNameContains(query, PageRequest.of(page, size))
                .map(ArtistModel::toDomain)
                .toList();
    }

    @Override
    public long countByNameContains(String query) {
        return artistJpaRepo.countByNameContains(query);
    }

    @Override
    public List<Artist> findByNameFullText(String query, int page, int size) {
        return artistJpaRepo.findByNameFullText(query, PageRequest.of(page, size))
                .map(ArtistModel::toDomain)
                .toList();
    }

    @Override
    public long countByNameFullText(String query) {
        return artistJpaRepo.countByNameFullText(query);
    }

    @Override
    public List<Artist> findByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return artistJpaRepo.findAllById(ids).stream()
                .map(ArtistModel::toDomain).toList();
    }
}
