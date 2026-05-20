package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.SystemPlaylistSong;
import com.example.ondas_be.domain.repoport.SystemPlaylistSongRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.SystemPlaylistSongJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.SystemPlaylistSongModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SystemPlaylistSongAdapter implements SystemPlaylistSongRepoPort {

    private final SystemPlaylistSongJpaRepo systemPlaylistSongJpaRepo;

    @Override
    public SystemPlaylistSong save(SystemPlaylistSong playlistSong) {
        return systemPlaylistSongJpaRepo.save(SystemPlaylistSongModel.fromDomain(playlistSong)).toDomain();
    }

    @Override
    public List<SystemPlaylistSong> findByPlaylistIdOrderByPosition(UUID playlistId) {
        return systemPlaylistSongJpaRepo.findByIdPlaylistIdOrderByPositionAsc(playlistId)
                .stream()
                .map(SystemPlaylistSongModel::toDomain)
                .toList();
    }

    @Override
    public List<UUID> findSongIdsByPlaylistId(UUID playlistId) {
        return systemPlaylistSongJpaRepo.findSongIdsByPlaylistIdOrderByPosition(playlistId);
    }

    @Override
    public long countByPlaylistId(UUID playlistId) {
        return systemPlaylistSongJpaRepo.countByIdPlaylistId(playlistId);
    }

    @Override
    public Integer findMaxPositionByPlaylistId(UUID playlistId) {
        return systemPlaylistSongJpaRepo.findMaxPositionByPlaylistId(playlistId);
    }

    @Override
    public boolean existsByPlaylistIdAndSongId(UUID playlistId, UUID songId) {
        return systemPlaylistSongJpaRepo.existsByIdPlaylistIdAndIdSongId(playlistId, songId);
    }

    @Override
    public void deleteByPlaylistIdAndSongId(UUID playlistId, UUID songId) {
        systemPlaylistSongJpaRepo.deleteByIdPlaylistIdAndIdSongId(playlistId, songId);
    }

    @Override
    @Transactional
    public void updateSongOrder(UUID playlistId, List<UUID> orderedSongIds) {
        int position = 1;
        for (UUID songId : orderedSongIds) {
            systemPlaylistSongJpaRepo.updatePosition(playlistId, songId, position++);
        }
    }
}
