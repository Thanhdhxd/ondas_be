package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.Album;
import com.example.ondas_be.domain.repoport.AlbumRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.AlbumJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.AlbumModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlbumAdapter implements AlbumRepoPort {

    private final AlbumJpaRepo albumJpaRepo;

    @Override
    public Album save(Album album) {
        return albumJpaRepo.save(AlbumModel.fromDomain(album)).toDomain();
    }

    @Override
    public Optional<Album> findById(UUID id) {
        return albumJpaRepo.findById(id).map(AlbumModel::toDomain);
    }

    @Override
    public List<Album> findAll(int page, int size) {
        return albumJpaRepo.findAll(PageRequest.of(page, size)).map(AlbumModel::toDomain).toList();
    }

    @Override
    public long countAll() {
        return albumJpaRepo.count();
    }

    @Override
    public void deleteById(UUID id) {
        albumJpaRepo.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return albumJpaRepo.existsById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return albumJpaRepo.existsBySlug(slug);
    }

    @Override
    public List<Album> findByTitleContains(String query, int page, int size) {
        return albumJpaRepo.findByTitleContains(query, PageRequest.of(page, size))
                .map(AlbumModel::toDomain)
                .toList();
    }

    @Override
    public long countByTitleContains(String query) {
        return albumJpaRepo.countByTitleContains(query);
    }

    @Override
    public List<Album> findByTitleFullText(String query, int page, int size) {
        return albumJpaRepo.findByTitleFullText(query, PageRequest.of(page, size))
                .map(AlbumModel::toDomain)
                .toList();
    }

    @Override
    public long countByTitleFullText(String query) {
        return albumJpaRepo.countByTitleFullText(query);
    }
}
