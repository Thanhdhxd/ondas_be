package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating plain/static lyrics of a song.
 */
@Data
public class UpdateStaticLyricsRequest {

    @Size(max = 10, message = "Language code must not exceed 10 characters")
    private String language;

    private String plainText;
}
