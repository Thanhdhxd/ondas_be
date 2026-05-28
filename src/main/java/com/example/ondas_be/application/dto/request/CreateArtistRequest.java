package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateArtistRequest {

    @NotBlank(message = "validation.not_blank")
    private String name;

    private String slug;

    private String bio;

    private String country;
}
