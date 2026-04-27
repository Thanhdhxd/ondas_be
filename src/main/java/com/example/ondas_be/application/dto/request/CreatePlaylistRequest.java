package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePlaylistRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private Boolean isPublic = false;
}
