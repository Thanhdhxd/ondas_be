package com.example.ondas_be.application.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class SystemPlaylistFilterRequest {

    private String query;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Boolean active;

    private int page = 0;
    private int size = 20;

    @JsonProperty("isActive")
    public Boolean getIsActive() {
        return active;
    }

    @JsonProperty("isActive")
    public void setIsActive(Boolean active) {
        this.active = active;
    }
}
