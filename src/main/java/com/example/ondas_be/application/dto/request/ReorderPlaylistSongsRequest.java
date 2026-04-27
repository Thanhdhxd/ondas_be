package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReorderPlaylistSongsRequest {

    @NotEmpty(message = "Song IDs are required")
    private List<UUID> songIds;
}
