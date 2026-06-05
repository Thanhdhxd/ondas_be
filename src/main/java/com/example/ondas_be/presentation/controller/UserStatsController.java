package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.response.UserListeningTimeResponse;
import com.example.ondas_be.application.dto.response.UserTopArtistResponse;
import com.example.ondas_be.application.dto.response.UserTopSongResponse;
import com.example.ondas_be.application.service.port.UserStatsServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats/me")
@RequiredArgsConstructor
public class UserStatsController {

    private final UserStatsServicePort userStatsServicePort;

    @GetMapping("/listening-time")
    public ResponseEntity<ApiResponse<UserListeningTimeResponse>> getMyListeningTime(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserListeningTimeResponse response = userStatsServicePort.getMyListeningTime(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/top-songs")
    public ResponseEntity<ApiResponse<List<UserTopSongResponse>>> getMyTopSongs(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit) {
        List<UserTopSongResponse> response = userStatsServicePort.getMyTopSongs(userDetails.getUsername(), limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/top-artists")
    public ResponseEntity<ApiResponse<List<UserTopArtistResponse>>> getMyTopArtists(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "10") int limit) {
        List<UserTopArtistResponse> response = userStatsServicePort.getMyTopArtists(userDetails.getUsername(), limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
