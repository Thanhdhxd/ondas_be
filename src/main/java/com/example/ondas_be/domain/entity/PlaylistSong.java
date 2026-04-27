package com.example.ondas_be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlaylistSong {

    private UUID playlistId;
    private UUID songId;
    private Integer position;
    private LocalDateTime addedAt;
}
