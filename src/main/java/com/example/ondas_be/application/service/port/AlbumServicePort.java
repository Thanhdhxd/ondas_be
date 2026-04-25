package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.request.CreateAlbumRequest;
import com.example.ondas_be.application.dto.request.UpdateAlbumRequest;
import com.example.ondas_be.application.dto.response.AlbumResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AlbumServicePort {

    AlbumResponse createAlbum(CreateAlbumRequest request, MultipartFile coverFile);

    AlbumResponse updateAlbum(UUID id, UpdateAlbumRequest request, MultipartFile coverFile);

    AlbumResponse getAlbumById(UUID id);

    List<AlbumResponse> getAllAlbums();

    PageResultDto<AlbumResponse> searchAlbumsByTitle(String query, String mode, int page, int size);

    void deleteAlbum(UUID id);

    List<AlbumResponse> getAlbumsByArtistId(UUID artistId);
}
