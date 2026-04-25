package com.example.ondas_be.application.mapper;

import com.example.ondas_be.application.dto.response.GenreResponse;
import com.example.ondas_be.application.dto.response.GenreSummaryResponse;
import com.example.ondas_be.domain.entity.Genre;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    GenreResponse toResponse(Genre genre);

    List<GenreResponse> toResponseList(List<Genre> genres);

    GenreSummaryResponse toSummaryResponse(Genre genre);

    List<GenreSummaryResponse> toSummaryResponseList(List<Genre> genres);
}
