package com.example.ondas_be.application.mapper;

import com.example.ondas_be.application.dto.response.PlaylistResponse;
import com.example.ondas_be.domain.entity.Playlist;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlaylistMapper {

    PlaylistResponse toResponse(Playlist playlist);
}
