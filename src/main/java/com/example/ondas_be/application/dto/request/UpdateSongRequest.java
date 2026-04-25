package com.example.ondas_be.application.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateSongRequest {

    private String title;
    private UUID albumId;
    private Integer trackNumber;
    private LocalDate releaseDate;
    private List<UUID> artistIds;
    private List<Long> genreIds;
    private Boolean active;
}
