package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AdminActivityLogFilterRequest;
import com.example.ondas_be.application.dto.response.AdminActivityLogResponse;
import com.example.ondas_be.application.service.port.AdminActivityLogServicePort;
import com.example.ondas_be.domain.entity.ActivityLog;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.ActivityLogRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminActivityLogService implements AdminActivityLogServicePort {

    private final ActivityLogRepoPort activityLogRepoPort;
    private final UserRepoPort userRepoPort;

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<AdminActivityLogResponse> getActivityLogs(AdminActivityLogFilterRequest filter) {
        String searchUser = (filter.getSearchUser() == null || filter.getSearchUser().isBlank())
                ? null : filter.getSearchUser().trim();
        String action = (filter.getAction() == null || filter.getAction().isBlank())
                ? null : filter.getAction().trim();

        Page<ActivityLog> page = activityLogRepoPort.findWithFilters(
                filter.getActorId(),
                searchUser,
                action,
                filter.getFrom(),
                filter.getTo(),
                PageRequest.of(filter.getPage(), filter.getSize())
        );

        // Fetch unique users to avoid N+1 queries
        List<UUID> actorIds = page.getContent().stream()
                .map(ActivityLog::getActorId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<UUID, User> userMap = new HashMap<>();
        for (UUID actorId : actorIds) {
            userRepoPort.findById(actorId).ifPresent(user -> userMap.put(actorId, user));
        }

        List<AdminActivityLogResponse> items = page.getContent().stream()
                .map(log -> {
                    User actor = log.getActorId() != null ? userMap.get(log.getActorId()) : null;
                    return AdminActivityLogResponse.builder()
                            .id(log.getId())
                            .actorId(log.getActorId())
                            .actorEmail(actor != null ? actor.getEmail() : null)
                            .actorDisplayName(actor != null ? actor.getDisplayName() : null)
                            .action(log.getAction())
                            .resourceType(log.getResourceType())
                            .resourceId(log.getResourceId())
                            .resourceName(log.getResourceName())
                            .metadata(log.getMetadata())
                            .ipAddress(log.getIpAddress())
                            .createdAt(log.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return PageResultDto.<AdminActivityLogResponse>builder()
                .items(items)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
