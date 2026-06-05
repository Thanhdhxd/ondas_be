package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ActivityLogRepoPort {

    ActivityLog save(ActivityLog log);

    Page<ActivityLog> findWithFilters(
            UUID actorId,
            String searchUser,
            String action,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);
}
