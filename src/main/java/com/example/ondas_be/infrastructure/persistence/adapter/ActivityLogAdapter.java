package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.ActivityLog;
import com.example.ondas_be.domain.repoport.ActivityLogRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.ActivityLogJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.ActivityLogModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ActivityLogAdapter implements ActivityLogRepoPort {

    private final ActivityLogJpaRepo activityLogJpaRepo;

    @Override
    public ActivityLog save(ActivityLog log) {
        ActivityLogModel model = ActivityLogModel.fromDomain(log);
        ActivityLogModel savedModel = activityLogJpaRepo.save(model);
        return savedModel.toDomain();
    }

    @Override
    public Page<ActivityLog> findWithFilters(
            UUID actorId,
            String searchUser,
            String action,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable) {
        return activityLogJpaRepo.findWithFilters(actorId, searchUser, action, from, to, pageable)
                .map(ActivityLogModel::toDomain);
    }
}
