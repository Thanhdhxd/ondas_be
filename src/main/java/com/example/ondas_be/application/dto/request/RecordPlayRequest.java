package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.UUID;

@Data
public class RecordPlayRequest {

    @NotNull(message = "validation.not_null")
    private UUID songId;

    @Pattern(
            regexp = "^(search|album|playlist|home|artist|favorites|history)$",
                message = "validation.pattern"
    )
    private String source;
}
