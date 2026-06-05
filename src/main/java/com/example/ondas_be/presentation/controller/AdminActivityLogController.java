package com.example.ondas_be.presentation.controller;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AdminActivityLogFilterRequest;
import com.example.ondas_be.application.dto.response.AdminActivityLogResponse;
import com.example.ondas_be.application.service.port.AdminActivityLogServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/activity-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminActivityLogController {

    private final AdminActivityLogServicePort adminActivityLogServicePort;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResultDto<AdminActivityLogResponse>>> getActivityLogs(
            @ModelAttribute AdminActivityLogFilterRequest filter) {
        PageResultDto<AdminActivityLogResponse> response = adminActivityLogServicePort.getActivityLogs(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
