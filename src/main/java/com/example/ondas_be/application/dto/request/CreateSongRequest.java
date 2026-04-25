package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateSongRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private UUID albumId;

    private Integer trackNumber;

    private LocalDate releaseDate;

    @NotEmpty(message = "Artist IDs are required")
    private List<UUID> artistIds;

    @NotEmpty(message = "Genre IDs are required")
    private List<Long> genreIds;
}
