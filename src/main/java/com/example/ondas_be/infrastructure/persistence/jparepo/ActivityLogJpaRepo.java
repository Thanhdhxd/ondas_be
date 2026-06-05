package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.ActivityLogModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ActivityLogJpaRepo extends JpaRepository<ActivityLogModel, Long> {

    // Sử dụng native query + CAST AS để tránh lỗi PostgreSQL không suy luận được kiểu dữ liệu
    // khi tham số truyền vào là null (lỗi "function lower(bytea) does not exist").
    @Query(value = """
        SELECT a.* FROM activity_logs a
        LEFT JOIN users u ON u.id = a.actor_id
        WHERE (CAST(:actorId AS uuid) IS NULL OR a.actor_id = :actorId)
          AND (CAST(:searchUser AS text) IS NULL
               OR LOWER(u.email)        LIKE LOWER(CONCAT('%', :searchUser, '%'))
               OR LOWER(u.display_name) LIKE LOWER(CONCAT('%', :searchUser, '%')))
          AND (CAST(:action AS text) IS NULL OR a.action = :action)
          AND (CAST(:fromTime AS timestamp) IS NULL OR a.created_at >= :fromTime)
          AND (CAST(:toTime AS timestamp) IS NULL OR a.created_at <= :toTime)
        ORDER BY a.created_at DESC
    """,
    countQuery = """
        SELECT COUNT(*) FROM activity_logs a
        LEFT JOIN users u ON u.id = a.actor_id
        WHERE (CAST(:actorId AS uuid) IS NULL OR a.actor_id = :actorId)
          AND (CAST(:searchUser AS text) IS NULL
               OR LOWER(u.email)        LIKE LOWER(CONCAT('%', :searchUser, '%'))
               OR LOWER(u.display_name) LIKE LOWER(CONCAT('%', :searchUser, '%')))
          AND (CAST(:action AS text) IS NULL OR a.action = :action)
          AND (CAST(:fromTime AS timestamp) IS NULL OR a.created_at >= :fromTime)
          AND (CAST(:toTime AS timestamp) IS NULL OR a.created_at <= :toTime)
    """,
    nativeQuery = true)
    Page<ActivityLogModel> findWithFilters(
            @Param("actorId") UUID actorId,
            @Param("searchUser") String searchUser,
            @Param("action") String action,
            @Param("fromTime") LocalDateTime from,
            @Param("toTime") LocalDateTime to,
            Pageable pageable);
}
