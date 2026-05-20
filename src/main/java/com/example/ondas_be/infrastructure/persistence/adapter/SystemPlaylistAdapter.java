package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.SystemPlaylist;
import com.example.ondas_be.domain.repoport.SystemPlaylistRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.SystemPlaylistJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.SystemPlaylistModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SystemPlaylistAdapter implements SystemPlaylistRepoPort {

    private final SystemPlaylistJpaRepo systemPlaylistJpaRepo;

    @Override
    public SystemPlaylist save(SystemPlaylist playlist) {
        return systemPlaylistJpaRepo.save(SystemPlaylistModel.fromDomain(playlist)).toDomain();
    }

    @Override
    public Optional<SystemPlaylist> findById(UUID id) {
        return systemPlaylistJpaRepo.findById(id).map(SystemPlaylistModel::toDomain);
    }

    @Override
    public List<SystemPlaylist> findActive(String query, int page, int size) {
        if (query != null && !query.isBlank()) {
            return systemPlaylistJpaRepo
                    .findByIsActiveTrueAndNameContainingIgnoreCase(query.trim(), PageRequest.of(page, size))
                    .map(SystemPlaylistModel::toDomain)
                    .toList();
        }
        return systemPlaylistJpaRepo.findByIsActiveTrue(PageRequest.of(page, size))
                .map(SystemPlaylistModel::toDomain)
                .toList();
    }

    @Override
    public long countActive(String query) {
        if (query != null && !query.isBlank()) {
            return systemPlaylistJpaRepo.countByIsActiveTrueAndNameContainingIgnoreCase(query.trim());
        }
        return systemPlaylistJpaRepo.countByIsActiveTrue();
    }

    @Override
    public List<SystemPlaylist> findAll(String query, Boolean isActive, int page, int size) {
        boolean hasQuery = query != null && !query.isBlank();
        if (isActive != null && hasQuery) {
            return systemPlaylistJpaRepo
                    .findByIsActiveAndNameContainingIgnoreCase(isActive, query.trim(), PageRequest.of(page, size))
                    .map(SystemPlaylistModel::toDomain)
                    .toList();
        }
        if (isActive != null) {
            return systemPlaylistJpaRepo.findByIsActive(isActive, PageRequest.of(page, size))
                    .map(SystemPlaylistModel::toDomain)
                    .toList();
        }
        if (hasQuery) {
            return systemPlaylistJpaRepo.findByNameContainingIgnoreCase(query.trim(), PageRequest.of(page, size))
                    .map(SystemPlaylistModel::toDomain)
                    .toList();
        }
        return systemPlaylistJpaRepo.findAll(PageRequest.of(page, size))
                .map(SystemPlaylistModel::toDomain)
                .toList();
    }

    @Override
    public long countAll(String query, Boolean isActive) {
        boolean hasQuery = query != null && !query.isBlank();
        if (isActive != null && hasQuery) {
            return systemPlaylistJpaRepo.countByIsActiveAndNameContainingIgnoreCase(isActive, query.trim());
        }
        if (isActive != null) {
            return systemPlaylistJpaRepo.countByIsActive(isActive);
        }
        if (hasQuery) {
            return systemPlaylistJpaRepo.countByNameContainingIgnoreCase(query.trim());
        }
        return systemPlaylistJpaRepo.count();
    }

    @Override
    public void deleteById(UUID id) {
        systemPlaylistJpaRepo.deleteById(id);
    }
}
