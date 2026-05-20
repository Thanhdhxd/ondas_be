package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.SystemPlaylistFilterRequest;
import com.example.ondas_be.application.dto.response.SystemPlaylistResponse;
import com.example.ondas_be.application.service.port.SystemPlaylistServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/system-playlists")
@RequiredArgsConstructor
public class SystemPlaylistController {

    private final SystemPlaylistServicePort systemPlaylistServicePort;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResultDto<SystemPlaylistResponse>>> getSystemPlaylists(
            @ModelAttribute SystemPlaylistFilterRequest filter) {
        PageResultDto<SystemPlaylistResponse> response = systemPlaylistServicePort.getActiveSystemPlaylists(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SystemPlaylistResponse>> getSystemPlaylistById(@PathVariable UUID id) {
        SystemPlaylistResponse response = systemPlaylistServicePort.getActiveSystemPlaylistById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
