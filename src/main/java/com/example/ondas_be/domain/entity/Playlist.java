package com.example.ondas_be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Playlist {

    private UUID id;
    private UUID userId;
    private String name;
    private String description;
    private String coverUrl;
    private boolean isPublic;
    private Integer totalSongs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
