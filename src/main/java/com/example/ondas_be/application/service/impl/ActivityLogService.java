package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.service.port.ActivityLogServicePort;
import com.example.ondas_be.domain.entity.ActivityLog;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.ActivityLogRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogService implements ActivityLogServicePort {

    private final ActivityLogRepoPort activityLogRepoPort;
    private final UserRepoPort userRepoPort;
    private final HttpServletRequest request;

    @Override
    public void recordLog(String action, String resourceType, UUID resourceId, String resourceName, String metadata) {
        UUID actorId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = null;
            if (authentication.getPrincipal() instanceof UserDetails) {
                email = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else if (authentication.getPrincipal() instanceof String) {
                email = (String) authentication.getPrincipal();
            }
            if (email != null) {
                actorId = userRepoPort.findByEmail(email)
                        .map(User::getId)
                        .orElse(null);
            }
        }

        String ipAddress = getClientIp(request);

        ActivityLog log = new ActivityLog(
                null,
                actorId,
                action,
                resourceType,
                resourceId,
                resourceName,
                metadata,
                ipAddress,
                LocalDateTime.now()
        );

        activityLogRepoPort.save(log);
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
