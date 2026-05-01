package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserRepoPort {
    Page<User> findAllWithFilters(String keyword, String role, Boolean active, Pageable pageable);
}
