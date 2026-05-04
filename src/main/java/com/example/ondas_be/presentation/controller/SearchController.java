package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.request.SearchFilterRequest;
import com.example.ondas_be.application.dto.response.SearchResponse;
import com.example.ondas_be.application.dto.response.SearchSuggestionResponse;
import com.example.ondas_be.application.service.port.SearchServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public ResponseEntity<ApiResponse<SearchResponse>> search(
            @ModelAttribute SearchFilterRequest filter,
            @AuthenticationPrincipal UserDetails userDetails) {
        SearchResponse result = searchServicePort.search(filter);
        // Lưu lịch sử tìm kiếm nếu user đã đăng nhập và query hợp lệ
        if (userDetails != null && filter.getQuery() != null && !filter.getQuery().isBlank()) {
            searchServicePort.saveSearchHistory(filter.getQuery().trim(), userDetails.getUsername());
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<SearchSuggestionResponse>> getSuggestions(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                searchServicePort.getSuggestions(userDetails.getUsername())));
    }

    @DeleteMapping("/history")
    public ResponseEntity<ApiResponse<Void>> clearSearchHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        searchServicePort.clearSearchHistory(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

