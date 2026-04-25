package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.request.CreateSongRequest;
import com.example.ondas_be.application.dto.request.SongFilterRequest;
import com.example.ondas_be.application.dto.request.UpdateSongRequest;
import com.example.ondas_be.application.dto.response.SongResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface SongServicePort {

    SongResponse createSong(CreateSongRequest request, MultipartFile audioFile, MultipartFile coverFile);

    SongResponse updateSong(UUID id, UpdateSongRequest request, MultipartFile audioFile, MultipartFile coverFile);

    SongResponse getSongById(UUID id);

    PageResultDto<SongResponse> getSongs(SongFilterRequest filter);

    void deleteSong(UUID id);
}
