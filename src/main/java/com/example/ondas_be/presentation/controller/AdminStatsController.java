package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.response.AdminDailyPlayResponse;
import com.example.ondas_be.application.dto.response.AdminDauMauResponse;
import com.example.ondas_be.application.dto.response.AdminTopArtistResponse;
import com.example.ondas_be.application.dto.response.AdminTopSongResponse;
import com.example.ondas_be.application.service.port.AdminStatsServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final AdminStatsServicePort adminStatsServicePort;

    @GetMapping("/top-songs")
    public ResponseEntity<ApiResponse<List<AdminTopSongResponse>>> getTopSongs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(ApiResponse.success(adminStatsServicePort.getTopSongs(from, to, limit)));
    }

    @GetMapping("/top-artists")
    public ResponseEntity<ApiResponse<List<AdminTopArtistResponse>>> getTopArtists(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(ApiResponse.success(adminStatsServicePort.getTopArtists(from, to, limit)));
    }

    @GetMapping("/plays-daily")
    public ResponseEntity<ApiResponse<List<AdminDailyPlayResponse>>> getDailyPlays(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(adminStatsServicePort.getDailyPlays(from, to)));
    }

    @GetMapping("/dau-mau")
    public ResponseEntity<ApiResponse<AdminDauMauResponse>> getDauMau(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(adminStatsServicePort.getDauMau(date)));
    }
}
