package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.SystemPlaylistSong;

import java.util.List;
import java.util.UUID;

public interface SystemPlaylistSongRepoPort {

    SystemPlaylistSong save(SystemPlaylistSong playlistSong);

    List<SystemPlaylistSong> findByPlaylistIdOrderByPosition(UUID playlistId);

    List<UUID> findSongIdsByPlaylistId(UUID playlistId);

    long countByPlaylistId(UUID playlistId);

    Integer findMaxPositionByPlaylistId(UUID playlistId);

    boolean existsByPlaylistIdAndSongId(UUID playlistId, UUID songId);

    void deleteByPlaylistIdAndSongId(UUID playlistId, UUID songId);

    void updateSongOrder(UUID playlistId, List<UUID> orderedSongIds);
}
