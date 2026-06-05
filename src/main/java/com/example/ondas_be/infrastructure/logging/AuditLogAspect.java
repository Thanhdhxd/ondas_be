package com.example.ondas_be.infrastructure.logging;

import com.example.ondas_be.application.dto.common.ApiResponse;
import com.example.ondas_be.application.service.port.ActivityLogServicePort;
import com.example.ondas_be.domain.entity.*;
import com.example.ondas_be.domain.repoport.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final ActivityLogServicePort activityLogService;
    private final UserRepoPort userRepoPort;
    private final SongRepoPort songRepoPort;
    private final ArtistRepoPort artistRepoPort;
    private final AlbumRepoPort albumRepoPort;
    private final SystemPlaylistRepoPort systemPlaylistRepoPort;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object profile(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        String resourceType = auditLog.resourceType();
        String action = auditLog.action();

        // 1. Try to extract ID from method arguments
        UUID resourceId = extractResourceId(null, args, paramNames);

        // 2. For DELETE/UPDATE/etc. actions, we pre-fetch the resource name from database before it gets deleted/modified
        String resourceName = null;
        if (resourceId != null) {
            resourceName = fetchResourceNameFromDb(resourceType, resourceId);
        }

        // 3. Serialize metadata (input arguments)
        String metadata = serializeMetadata(args, paramNames);

        // Execute the actual controller method
        Object result = joinPoint.proceed();

        // 4. For non-delete, if resourceName is still null (e.g. CREATE operation), or we want to get the updated name, extract from result
        if (resourceName == null || action.startsWith("CREATE")) {
            String extractedName = extractResourceName(result);
            if (extractedName != null) {
                resourceName = extractedName;
            }
        }

        // 5. If resourceId was not in method arguments (e.g. CREATE operation), extract from result
        if (resourceId == null) {
            UUID extractedId = extractResourceId(result, null, null);
            if (extractedId != null) {
                resourceId = extractedId;
            }
        }

        // Save log
        try {
            activityLogService.recordLog(action, resourceType, resourceId, resourceName, metadata);
        } catch (Exception e) {
            // Do not fail the API call if logging fails
            e.printStackTrace();
        }

        return result;
    }

    private UUID extractResourceId(Object result, Object[] args, String[] paramNames) {
        // Try from method arguments first (e.g. PathVariable ID)
        if (paramNames != null && args != null) {
            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equalsIgnoreCase("id") || paramNames[i].equalsIgnoreCase("songId")) {
                    if (args[i] instanceof UUID) {
                        return (UUID) args[i];
                    }
                }
            }
        }

        // Try from return value (ApiResponse -> data -> id)
        if (result instanceof ResponseEntity) {
            result = ((ResponseEntity<?>) result).getBody();
        }
        if (result instanceof ApiResponse) {
            result = ((ApiResponse<?>) result).getData();
        }

        if (result != null) {
            try {
                Method getIdMethod = result.getClass().getMethod("getId");
                Object val = getIdMethod.invoke(result);
                if (val instanceof UUID) {
                    return (UUID) val;
                }
            } catch (Exception ignored) {}
        }

        return null;
    }

    private String extractResourceName(Object result) {
        if (result instanceof ResponseEntity) {
            result = ((ResponseEntity<?>) result).getBody();
        }
        if (result instanceof ApiResponse) {
            result = ((ApiResponse<?>) result).getData();
        }
        if (result == null) {
            return null;
        }

        // Try getTitle(), getName(), getDisplayName(), getEmail()
        for (String methodName : List.of("getTitle", "getName", "getDisplayName", "getEmail")) {
            try {
                Method method = result.getClass().getMethod(methodName);
                Object val = method.invoke(result);
                if (val != null) {
                    return val.toString();
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String fetchResourceNameFromDb(String resourceType, UUID id) {
        if (id == null) {
            return null;
        }
        try {
            switch (resourceType.toUpperCase()) {
                case "USER":
                    return userRepoPort.findById(id).map(User::getDisplayName).orElse(null);
                case "SONG":
                    return songRepoPort.findById(id).map(Song::getTitle).orElse(null);
                case "ARTIST":
                    return artistRepoPort.findById(id).map(Artist::getName).orElse(null);
                case "ALBUM":
                    return albumRepoPort.findById(id).map(Album::getTitle).orElse(null);
                case "SYSTEM_PLAYLIST":
                    return systemPlaylistRepoPort.findById(id).map(SystemPlaylist::getName).orElse(null);
            }
        } catch (Exception e) {
            // Ignore to not disrupt execution flow
        }
        return null;
    }

    private String serializeMetadata(Object[] args, String[] paramNames) {
        try {
            Map<String, Object> params = new HashMap<>();
            if (paramNames != null && args != null) {
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg == null) continue;

                    String name = paramNames[i];
                    // Skip large binary objects, HttpServletRequest, response, and security objects
                    if (arg instanceof org.springframework.web.multipart.MultipartFile ||
                        arg instanceof jakarta.servlet.http.HttpServletRequest ||
                        arg instanceof jakarta.servlet.http.HttpServletResponse ||
                        arg instanceof org.springframework.security.core.userdetails.UserDetails ||
                        arg instanceof org.springframework.security.core.Authentication ||
                        arg instanceof org.springframework.validation.BindingResult ||
                        arg.getClass().getName().startsWith("org.springframework")) {
                        continue;
                    }
                    params.put(name, arg);
                }
            }
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }
}
