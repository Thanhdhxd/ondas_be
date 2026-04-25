package com.example.ondas_be.application.mapper;

import com.example.ondas_be.application.dto.response.ArtistResponse;
import com.example.ondas_be.application.dto.response.ArtistSummaryResponse;
import com.example.ondas_be.domain.entity.Artist;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ArtistMapper {

    ArtistResponse toResponse(Artist artist);

    List<ArtistResponse> toResponseList(List<Artist> artists);

    ArtistSummaryResponse toSummaryResponse(Artist artist);

    List<ArtistSummaryResponse> toSummaryResponseList(List<Artist> artists);
}
