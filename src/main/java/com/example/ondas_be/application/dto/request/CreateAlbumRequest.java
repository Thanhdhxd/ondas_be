package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateAlbumRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String slug;

    private LocalDate releaseDate;

    private String albumType;

    private String description;

    @NotEmpty(message = "Artist IDs are required")
    private List<UUID> artistIds;

    private List<UUID> songIds;
}
