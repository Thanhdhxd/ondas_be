package com.example.ondas_be.application.mapper;

import com.example.ondas_be.application.dto.response.SystemPlaylistResponse;
import com.example.ondas_be.domain.entity.SystemPlaylist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SystemPlaylistMapper {

    @Mapping(target = "active", source = "active")
    SystemPlaylistResponse toResponse(SystemPlaylist playlist);
}
