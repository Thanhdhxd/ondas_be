package com.example.ondas_be.application.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateAlbumRequest {

    private String title;
    private String slug;
    private LocalDate releaseDate;
    private String albumType;
    private String description;
    private List<UUID> artistIds;
    private List<UUID> songIds;
}
