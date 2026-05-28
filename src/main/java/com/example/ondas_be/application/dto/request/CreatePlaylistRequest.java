package com.example.ondas_be.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePlaylistRequest {

    @NotBlank(message = "validation.not_blank")
    @Size(max = 100, message = "validation.size.max")
    private String name;

    private String description;

    private Boolean isPublic = false;

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }
}
