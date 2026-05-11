package com.example.ondas_be.application.mapper;

import com.example.ondas_be.application.dto.response.SyncedLyricsLineResponse;
import com.example.ondas_be.domain.entity.SyncedLyricsLine;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SyncedLyricsMapper {

    SyncedLyricsLineResponse toResponse(SyncedLyricsLine line);

    List<SyncedLyricsLineResponse> toResponseList(List<SyncedLyricsLine> lines);
}
