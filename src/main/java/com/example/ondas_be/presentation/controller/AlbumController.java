package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AlbumFilterRequest;
import com.example.ondas_be.application.dto.request.CreateAlbumRequest;
import com.example.ondas_be.application.dto.request.UpdateAlbumRequest;
import com.example.ondas_be.application.dto.response.AlbumResponse;
import com.example.ondas_be.application.service.port.AlbumServicePort;
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
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumServicePort albumServicePort;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<AlbumResponse>> createAlbum(
            @Valid @RequestPart("data") CreateAlbumRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile) {
        AlbumResponse response = albumServicePort.createAlbum(request, coverFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<AlbumResponse>> updateAlbum(
            @PathVariable UUID id,
            @RequestPart("data") UpdateAlbumRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile) {
        AlbumResponse response = albumServicePort.updateAlbum(id, request, coverFile);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AlbumResponse>> getAlbumById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(albumServicePort.getAlbumById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResultDto<AlbumResponse>>> getAlbums(
            @ModelAttribute AlbumFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(albumServicePort.getAlbums(filter)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteAlbum(@PathVariable UUID id) {
        albumServicePort.deleteAlbum(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
