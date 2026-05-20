package com.example.ondas_be.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemPlaylistResponse {

    private UUID id;
    private String name;
    private String description;
    private String coverUrl;

    @Getter(AccessLevel.NONE)
    private boolean active;

    private Integer totalSongs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SystemPlaylistSongResponse> songs;

    @JsonProperty("isActive")
    public boolean getActive() {
        return active;
    }

    @JsonProperty("isActive")
    public void setActive(boolean active) {
        this.active = active;
    }
}
