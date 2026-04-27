package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.PlaylistSongId;
import com.example.ondas_be.infrastructure.persistence.model.PlaylistSongModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlaylistSongJpaRepo extends JpaRepository<PlaylistSongModel, PlaylistSongId> {

    List<PlaylistSongModel> findByIdPlaylistIdOrderByPositionAsc(UUID playlistId);

    @Query("select ps.id.songId from PlaylistSongModel ps where ps.id.playlistId = :playlistId order by ps.position asc")
    List<UUID> findSongIdsByPlaylistIdOrderByPosition(@Param("playlistId") UUID playlistId);

    long countByIdPlaylistId(UUID playlistId);

    @Query("select coalesce(max(ps.position), 0) from PlaylistSongModel ps where ps.id.playlistId = :playlistId")
    Integer findMaxPositionByPlaylistId(@Param("playlistId") UUID playlistId);

    boolean existsByIdPlaylistIdAndIdSongId(UUID playlistId, UUID songId);

    void deleteByIdPlaylistIdAndIdSongId(UUID playlistId, UUID songId);

    @Modifying
    @Query("update PlaylistSongModel ps set ps.position = :position where ps.id.playlistId = :playlistId and ps.id.songId = :songId")
    int updatePosition(@Param("playlistId") UUID playlistId, @Param("songId") UUID songId,
            @Param("position") Integer position);
}
