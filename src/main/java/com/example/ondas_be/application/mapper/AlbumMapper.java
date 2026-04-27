package com.example.ondas_be.application.mapper;

import com.example.ondas_be.application.dto.response.AlbumResponse;
import com.example.ondas_be.domain.entity.Album;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AlbumMapper {

    @Mapping(target = "tracklist", ignore = true)
    @Mapping(target = "artists", ignore = true)
    AlbumResponse toResponse(Album album);

    List<AlbumResponse> toResponseList(List<Album> albums);
}
