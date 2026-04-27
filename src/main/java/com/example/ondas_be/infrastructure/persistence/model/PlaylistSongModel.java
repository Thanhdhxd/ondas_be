package com.example.ondas_be.infrastructure.persistence.model;

import com.example.ondas_be.domain.entity.PlaylistSong;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "playlist_songs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSongModel {

    @EmbeddedId
    private PlaylistSongId id;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    void prePersist() {
        if (this.addedAt == null) {
            this.addedAt = LocalDateTime.now();
        }
    }

    public PlaylistSong toDomain() {
        return new PlaylistSong(
                id.getPlaylistId(),
                id.getSongId(),
                position,
                addedAt
        );
    }

    public static PlaylistSongModel fromDomain(PlaylistSong playlistSong) {
        return PlaylistSongModel.builder()
                .id(new PlaylistSongId(playlistSong.getPlaylistId(), playlistSong.getSongId()))
                .position(playlistSong.getPosition())
                .addedAt(playlistSong.getAddedAt())
                .build();
    }
}
