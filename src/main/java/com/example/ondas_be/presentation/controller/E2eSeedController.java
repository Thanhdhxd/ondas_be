package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.service.port.E2eSeedServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/e2e")
@Profile("e2e")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class E2eSeedController {

    private final E2eSeedServicePort e2eSeedServicePort;

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetAndSeed() {
        e2eSeedServicePort.resetAndSeed();
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<Void>> seedIfEmpty() {
        e2eSeedServicePort.seedIfEmpty();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
