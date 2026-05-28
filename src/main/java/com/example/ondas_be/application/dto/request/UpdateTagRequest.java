package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateTagRequest {

    private String name;

    private String type;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "validation.pattern")
    private String colorHex;
}
