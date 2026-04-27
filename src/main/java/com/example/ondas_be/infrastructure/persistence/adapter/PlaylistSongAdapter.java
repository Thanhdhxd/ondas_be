package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.PlaylistSong;
import com.example.ondas_be.domain.repoport.PlaylistSongRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.PlaylistSongJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.PlaylistSongModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlaylistSongAdapter implements PlaylistSongRepoPort {

    private final PlaylistSongJpaRepo playlistSongJpaRepo;

    @Override
    public PlaylistSong save(PlaylistSong playlistSong) {
        return playlistSongJpaRepo.save(PlaylistSongModel.fromDomain(playlistSong)).toDomain();
    }

    @Override
    public List<PlaylistSong> findByPlaylistIdOrderByPosition(UUID playlistId) {
        return playlistSongJpaRepo.findByIdPlaylistIdOrderByPositionAsc(playlistId)
                .stream()
                .map(PlaylistSongModel::toDomain)
                .toList();
    }

    @Override
    public List<UUID> findSongIdsByPlaylistId(UUID playlistId) {
        return playlistSongJpaRepo.findSongIdsByPlaylistIdOrderByPosition(playlistId);
    }

    @Override
    public long countByPlaylistId(UUID playlistId) {
        return playlistSongJpaRepo.countByIdPlaylistId(playlistId);
    }

    @Override
    public Integer findMaxPositionByPlaylistId(UUID playlistId) {
        return playlistSongJpaRepo.findMaxPositionByPlaylistId(playlistId);
    }

    @Override
    public boolean existsByPlaylistIdAndSongId(UUID playlistId, UUID songId) {
        return playlistSongJpaRepo.existsByIdPlaylistIdAndIdSongId(playlistId, songId);
    }

    @Override
    public void deleteByPlaylistIdAndSongId(UUID playlistId, UUID songId) {
        playlistSongJpaRepo.deleteByIdPlaylistIdAndIdSongId(playlistId, songId);
    }

    @Override
    @Transactional
    public void updateSongOrder(UUID playlistId, List<UUID> orderedSongIds) {
        int position = 1;
        for (UUID songId : orderedSongIds) {
            playlistSongJpaRepo.updatePosition(playlistId, songId, position++);
        }
    }
}
