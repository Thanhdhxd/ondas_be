package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating plain/static lyrics of a song.
 */
@Data
public class UpdateStaticLyricsRequest {

    @Size(max = 10, message = "validation.size.max")
    private String language;

    private String plainText;
}
