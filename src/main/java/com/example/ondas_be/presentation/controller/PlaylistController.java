package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AddSongToPlaylistRequest;
import com.example.ondas_be.application.dto.request.CreatePlaylistRequest;
import com.example.ondas_be.application.dto.request.PlaylistFilterRequest;
import com.example.ondas_be.application.dto.request.ReorderPlaylistSongsRequest;
import com.example.ondas_be.application.dto.request.UpdatePlaylistRequest;
import com.example.ondas_be.application.dto.response.PlaylistResponse;
import com.example.ondas_be.application.service.port.PlaylistServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistServicePort playlistServicePort;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PlaylistResponse>> createPlaylist(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestPart("data") CreatePlaylistRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile) {
        PlaylistResponse response = playlistServicePort.createPlaylist(userDetails.getUsername(), request, coverFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PlaylistResponse>> updatePlaylist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @RequestPart("data") UpdatePlaylistRequest request,
            @RequestPart(value = "cover", required = false) MultipartFile coverFile) {
        PlaylistResponse response = playlistServicePort.updatePlaylist(userDetails.getUsername(), id, request, coverFile);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> getPlaylistById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        PlaylistResponse response = playlistServicePort.getPlaylistById(extractEmail(userDetails), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResultDto<PlaylistResponse>>> getPlaylists(
            @ModelAttribute PlaylistFilterRequest filter,
            @AuthenticationPrincipal UserDetails userDetails) {
        PageResultDto<PlaylistResponse> response = playlistServicePort.getPlaylists(extractEmail(userDetails), filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlaylist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        playlistServicePort.deletePlaylist(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/songs")
    public ResponseEntity<ApiResponse<PlaylistResponse>> addSongToPlaylist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody AddSongToPlaylistRequest request) {
        PlaylistResponse response = playlistServicePort.addSongToPlaylist(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> removeSongFromPlaylist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @PathVariable UUID songId) {
        PlaylistResponse response = playlistServicePort.removeSongFromPlaylist(userDetails.getUsername(), id, songId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/songs/reorder")
    public ResponseEntity<ApiResponse<PlaylistResponse>> reorderPlaylistSongs(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody ReorderPlaylistSongsRequest request) {
        PlaylistResponse response = playlistServicePort.reorderPlaylistSongs(userDetails.getUsername(), id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String extractEmail(UserDetails userDetails) {
        return userDetails != null ? userDetails.getUsername() : null;
    }
}
