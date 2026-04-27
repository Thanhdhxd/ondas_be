package com.example.ondas_be.infrastructure.persistence.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSongId implements Serializable {

    @Column(name = "playlist_id")
    private UUID playlistId;

    @Column(name = "song_id")
    private UUID songId;
}
