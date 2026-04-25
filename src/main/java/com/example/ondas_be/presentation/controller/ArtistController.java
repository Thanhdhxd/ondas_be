package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.ArtistFilterRequest;
import com.example.ondas_be.application.dto.request.CreateArtistRequest;
import com.example.ondas_be.application.dto.request.UpdateArtistRequest;
import com.example.ondas_be.application.dto.response.ArtistResponse;
import com.example.ondas_be.application.service.port.ArtistServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/artists")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistServicePort artistServicePort;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<ArtistResponse>> createArtist(
            @Valid @RequestPart("data") CreateArtistRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        ArtistResponse response = artistServicePort.createArtist(request, avatarFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<ArtistResponse>> updateArtist(
            @PathVariable UUID id,
            @RequestPart("data") UpdateArtistRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatarFile) {
        ArtistResponse response = artistServicePort.updateArtist(id, request, avatarFile);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArtistResponse>> getArtistById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(artistServicePort.getArtistById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResultDto<ArtistResponse>>> getArtists(
            @ModelAttribute ArtistFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(artistServicePort.getArtists(filter)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteArtist(@PathVariable UUID id) {
        artistServicePort.deleteArtist(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
