package com.example.ondas_be.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistResponse {

    private UUID id;
    private UUID userId;
    private String name;
    private String description;
    private String coverUrl;
    private boolean isPublic;
    private Integer totalSongs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PlaylistSongResponse> songs;
}
