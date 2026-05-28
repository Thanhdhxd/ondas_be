package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReorderSystemPlaylistSongsRequest {

    @NotEmpty(message = "validation.not_empty")
    private List<UUID> songIds;
}
