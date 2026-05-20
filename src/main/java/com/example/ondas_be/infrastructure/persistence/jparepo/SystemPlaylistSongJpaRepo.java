package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.SystemPlaylistSongId;
import com.example.ondas_be.infrastructure.persistence.model.SystemPlaylistSongModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SystemPlaylistSongJpaRepo extends JpaRepository<SystemPlaylistSongModel, SystemPlaylistSongId> {

    List<SystemPlaylistSongModel> findByIdPlaylistIdOrderByPositionAsc(UUID playlistId);

    @Query("select ps.id.songId from SystemPlaylistSongModel ps where ps.id.playlistId = :playlistId order by ps.position asc")
    List<UUID> findSongIdsByPlaylistIdOrderByPosition(@Param("playlistId") UUID playlistId);

    long countByIdPlaylistId(UUID playlistId);

    @Query("select coalesce(max(ps.position), 0) from SystemPlaylistSongModel ps where ps.id.playlistId = :playlistId")
    Integer findMaxPositionByPlaylistId(@Param("playlistId") UUID playlistId);

    boolean existsByIdPlaylistIdAndIdSongId(UUID playlistId, UUID songId);

    void deleteByIdPlaylistIdAndIdSongId(UUID playlistId, UUID songId);

    @Modifying
    @Query("update SystemPlaylistSongModel ps set ps.position = :position where ps.id.playlistId = :playlistId and ps.id.songId = :songId")
    int updatePosition(@Param("playlistId") UUID playlistId, @Param("songId") UUID songId,
            @Param("position") Integer position);
}
