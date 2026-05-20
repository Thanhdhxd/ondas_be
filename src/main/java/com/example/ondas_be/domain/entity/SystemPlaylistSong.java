package com.example.ondas_be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class SystemPlaylistSong {

    private UUID playlistId;
    private UUID songId;
    private Integer position;
}
