package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.RecordPlayRequest;
import com.example.ondas_be.application.dto.response.PlayHistoryResponse;
import com.example.ondas_be.application.service.port.PlayHistoryServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/play-history")
@RequiredArgsConstructor
public class PlayHistoryController {

    private final PlayHistoryServicePort playHistoryServicePort;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResultDto<PlayHistoryResponse>>> getMyHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResultDto<PlayHistoryResponse> result =
                playHistoryServicePort.getMyHistory(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearMyHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        playHistoryServicePort.clearMyHistory(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHistoryEntry(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        playHistoryServicePort.deleteHistoryEntry(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> recordPlay(
            @Valid @RequestBody RecordPlayRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        playHistoryServicePort.recordPlay(request.getSongId(), userDetails.getUsername(), request.getSource());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
