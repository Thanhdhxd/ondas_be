package com.example.ondas_be.infrastructure.persistence.model;

import com.example.ondas_be.domain.entity.SystemPlaylistSong;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "system_playlist_songs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemPlaylistSongModel {

    @EmbeddedId
    private SystemPlaylistSongId id;

    @Column(nullable = false)
    private Integer position;

    public SystemPlaylistSong toDomain() {
        return new SystemPlaylistSong(
                id.getPlaylistId(),
                id.getSongId(),
                position
        );
    }

    public static SystemPlaylistSongModel fromDomain(SystemPlaylistSong playlistSong) {
        return SystemPlaylistSongModel.builder()
                .id(new SystemPlaylistSongId(playlistSong.getPlaylistId(), playlistSong.getSongId()))
                .position(playlistSong.getPosition())
                .build();
    }
}
