package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.request.ArtistFilterRequest;
import com.example.ondas_be.application.dto.request.CreateArtistRequest;
import com.example.ondas_be.application.dto.request.UpdateArtistRequest;
import com.example.ondas_be.application.dto.response.ArtistResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ArtistServicePort {

    ArtistResponse createArtist(CreateArtistRequest request, MultipartFile avatarFile);

    ArtistResponse updateArtist(UUID id, UpdateArtistRequest request, MultipartFile avatarFile);

    ArtistResponse getArtistById(UUID id);

    PageResultDto<ArtistResponse> getArtists(ArtistFilterRequest filter);

    void deleteArtist(UUID id);
}
