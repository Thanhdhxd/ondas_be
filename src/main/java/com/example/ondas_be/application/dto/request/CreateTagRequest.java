package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateTagRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String type;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be in #RRGGBB format")
    private String colorHex;
}
