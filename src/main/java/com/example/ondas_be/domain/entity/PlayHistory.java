package com.example.ondas_be.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlayHistory {

    private Long id;
    private UUID userId;
    private UUID songId;
    private LocalDateTime playedAt;
    private String source;
    private Integer durationPlayedSeconds;
    private Boolean completed;
}
