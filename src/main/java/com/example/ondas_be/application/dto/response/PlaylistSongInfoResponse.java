package com.example.ondas_be.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSongInfoResponse {

    private UUID id;
    private String title;
    private String coverUrl;
    private Integer durationSeconds;
    private String audioUrl;
}
