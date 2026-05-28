package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddSongToPlaylistRequest {

    @NotNull(message = "validation.not_null")
    private UUID songId;
}
