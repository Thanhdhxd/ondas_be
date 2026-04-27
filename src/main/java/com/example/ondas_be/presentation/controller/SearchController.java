package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.request.SearchFilterRequest;
import com.example.ondas_be.application.dto.response.SearchResponse;
import com.example.ondas_be.application.service.port.SearchServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchServicePort searchServicePort;

    @GetMapping
    public ResponseEntity<ApiResponse<SearchResponse>> search(@ModelAttribute SearchFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(searchServicePort.search(filter)));
    }
}
