package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AddSongToSystemPlaylistRequest;
import com.example.ondas_be.application.dto.request.CreateSystemPlaylistRequest;
import com.example.ondas_be.application.dto.request.ReorderSystemPlaylistSongsRequest;
import com.example.ondas_be.application.dto.request.SystemPlaylistFilterRequest;
import com.example.ondas_be.application.dto.request.UpdateSystemPlaylistRequest;
import com.example.ondas_be.application.dto.response.SystemPlaylistResponse;
import com.example.ondas_be.application.service.port.SystemPlaylistServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/system-playlists")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
public class AdminSystemPlaylistController {

    private final SystemPlaylistServicePort systemPlaylistServicePort;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SystemPlaylistResponse>> createSystemPlaylist(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestPart("data") CreateSystemPlaylistRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile) {
        SystemPlaylistResponse response = systemPlaylistServicePort.createSystemPlaylist(
                userDetails.getUsername(),
                request,
                coverFile
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SystemPlaylistResponse>> updateSystemPlaylist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestPart("data") UpdateSystemPlaylistRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile) {
        SystemPlaylistResponse response = systemPlaylistServicePort.updateSystemPlaylist(
                userDetails.getUsername(),
                id,
                request,
                coverFile
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResultDto<SystemPlaylistResponse>>> getSystemPlaylists(
            @ModelAttribute SystemPlaylistFilterRequest filter) {
        PageResultDto<SystemPlaylistResponse> response = systemPlaylistServicePort.getSystemPlaylists(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SystemPlaylistResponse>> getSystemPlaylistById(@PathVariable UUID id) {
        SystemPlaylistResponse response = systemPlaylistServicePort.getSystemPlaylistById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSystemPlaylist(@PathVariable UUID id) {
        systemPlaylistServicePort.deleteSystemPlaylist(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/songs")
    public ResponseEntity<ApiResponse<SystemPlaylistResponse>> addSongToSystemPlaylist(
            @PathVariable UUID id,
            @Valid @RequestBody AddSongToSystemPlaylistRequest request) {
        SystemPlaylistResponse response = systemPlaylistServicePort.addSongToSystemPlaylist(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<ApiResponse<SystemPlaylistResponse>> removeSongFromSystemPlaylist(
            @PathVariable UUID id,
            @PathVariable UUID songId) {
        SystemPlaylistResponse response = systemPlaylistServicePort.removeSongFromSystemPlaylist(id, songId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/songs/reorder")
    public ResponseEntity<ApiResponse<SystemPlaylistResponse>> reorderSystemPlaylistSongs(
            @PathVariable UUID id,
            @Valid @RequestBody ReorderSystemPlaylistSongsRequest request) {
        SystemPlaylistResponse response = systemPlaylistServicePort.reorderSystemPlaylistSongs(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
