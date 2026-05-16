package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.CreateTagRequest;
import com.example.ondas_be.application.dto.request.UpdateTagRequest;
import com.example.ondas_be.application.dto.response.TagResponse;
import com.example.ondas_be.application.service.port.TagServicePort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagServicePort tagServicePort;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(@Valid @RequestBody CreateTagRequest request) {
        TagResponse response = tagServicePort.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequest request) {
        TagResponse response = tagServicePort.updateTag(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> getTagById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(tagServicePort.getTagById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags(
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(ApiResponse.success(tagServicePort.getAllTags(type)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResultDto<TagResponse>>> searchTagsByName(
            @RequestParam String query,
            @RequestParam(defaultValue = "contains") String mode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(tagServicePort.searchTagsByName(query, mode, page, size)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long id) {
        tagServicePort.deleteTag(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
