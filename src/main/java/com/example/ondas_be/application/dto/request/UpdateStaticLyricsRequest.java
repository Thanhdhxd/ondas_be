package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateStaticLyricsRequest {
    @NotBlank(message = "Lyrics text cannot be blank")
    private String plainText;
}
