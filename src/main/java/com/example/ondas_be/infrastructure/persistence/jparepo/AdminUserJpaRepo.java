package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AdminUserJpaRepo extends JpaRepository<UserModel, UUID> {

    // Dùng native query + CAST AS text để PostgreSQL xác định đúng kiểu dữ liệu khi truyền null,
    // tránh lỗi "function lower(bytea) does not exist" xảy ra với JPQL.
    @Query(value = """
        SELECT * FROM users u
        WHERE (CAST(:keyword AS text) IS NULL
               OR LOWER(u.email)        LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.display_name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (CAST(:role AS text) IS NULL OR u.role = :role)
          AND (CAST(:active AS text)   IS NULL OR u.is_active = :active)
        ORDER BY u.created_at DESC
    """,
    countQuery = """
        SELECT COUNT(*) FROM users u
        WHERE (CAST(:keyword AS text) IS NULL
               OR LOWER(u.email)        LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.display_name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (CAST(:role AS text) IS NULL OR u.role = :role)
          AND (CAST(:active AS text)   IS NULL OR u.is_active = :active)
    """,
    nativeQuery = true)
    Page<UserModel> findAllWithFilters(
            @Param("keyword") String keyword,
            @Param("role")    String role,
            @Param("active")  Boolean active,
            Pageable pageable);
}
