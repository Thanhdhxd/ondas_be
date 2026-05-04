package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.response.FavoriteSongResponse;
import com.example.ondas_be.application.service.port.FavoriteServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteServicePort favoriteServicePort;

    @PostMapping("/{songId}")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @PathVariable UUID songId,
            @AuthenticationPrincipal UserDetails userDetails) {
        favoriteServicePort.addFavorite(userDetails.getUsername(), songId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable UUID songId,
            @AuthenticationPrincipal UserDetails userDetails) {
        favoriteServicePort.removeFavorite(userDetails.getUsername(), songId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{songId}/status")
    public ResponseEntity<ApiResponse<Boolean>> isFavorite(
            @PathVariable UUID songId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boolean result = favoriteServicePort.isFavorite(userDetails.getUsername(), songId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResultDto<FavoriteSongResponse>>> getFavorites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        PageResultDto<FavoriteSongResponse> result =
                favoriteServicePort.getFavorites(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
