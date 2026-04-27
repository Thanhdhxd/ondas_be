package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.Playlist;
import com.example.ondas_be.domain.repoport.PlaylistRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.PlaylistJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.PlaylistModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlaylistAdapter implements PlaylistRepoPort {

    private final PlaylistJpaRepo playlistJpaRepo;

    @Override
    public Playlist save(Playlist playlist) {
        return playlistJpaRepo.save(PlaylistModel.fromDomain(playlist)).toDomain();
    }

    @Override
    public Optional<Playlist> findById(UUID id) {
        return playlistJpaRepo.findById(id).map(PlaylistModel::toDomain);
    }

    @Override
    public List<Playlist> findPublic(String query, int page, int size) {
        if (query != null && !query.isBlank()) {
            return playlistJpaRepo.findByIsPublicTrueAndNameContainingIgnoreCase(query.trim(), PageRequest.of(page, size))
                    .map(PlaylistModel::toDomain)
                    .toList();
        }
        return playlistJpaRepo.findByIsPublicTrue(PageRequest.of(page, size)).map(PlaylistModel::toDomain).toList();
    }

    @Override
    public long countPublic(String query) {
        if (query != null && !query.isBlank()) {
            return playlistJpaRepo.countByIsPublicTrueAndNameContainingIgnoreCase(query.trim());
        }
        return playlistJpaRepo.countByIsPublicTrue();
    }

    @Override
    public List<Playlist> findByUserId(UUID userId, Boolean isPublic, String query, int page, int size) {
        boolean hasQuery = query != null && !query.isBlank();
        if (isPublic != null && hasQuery) {
            return playlistJpaRepo.findByUserIdAndIsPublicAndNameContainingIgnoreCase(
                    userId,
                    isPublic,
                    query.trim(),
                    PageRequest.of(page, size)
            ).map(PlaylistModel::toDomain).toList();
        }

        if (isPublic != null) {
            return playlistJpaRepo.findByUserIdAndIsPublic(userId, isPublic, PageRequest.of(page, size))
                    .map(PlaylistModel::toDomain)
                    .toList();
        }

        if (hasQuery) {
            return playlistJpaRepo.findByUserIdAndNameContainingIgnoreCase(userId, query.trim(), PageRequest.of(page, size))
                    .map(PlaylistModel::toDomain)
                    .toList();
        }

        return playlistJpaRepo.findByUserId(userId, PageRequest.of(page, size)).map(PlaylistModel::toDomain).toList();
    }

    @Override
    public long countByUserId(UUID userId, Boolean isPublic, String query) {
        boolean hasQuery = query != null && !query.isBlank();
        if (isPublic != null && hasQuery) {
            return playlistJpaRepo.countByUserIdAndIsPublicAndNameContainingIgnoreCase(userId, isPublic, query.trim());
        }
        if (isPublic != null) {
            return playlistJpaRepo.countByUserIdAndIsPublic(userId, isPublic);
        }
        if (hasQuery) {
            return playlistJpaRepo.countByUserIdAndNameContainingIgnoreCase(userId, query.trim());
        }
        return playlistJpaRepo.countByUserId(userId);
    }

    @Override
    public void deleteById(UUID id) {
        playlistJpaRepo.deleteById(id);
    }
}
