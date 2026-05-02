package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.AdminUserRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.AdminUserJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserAdapter implements AdminUserRepoPort {

    private final AdminUserJpaRepo adminUserJpaRepo;

    @Override
    public Page<User> findAllWithFilters(String keyword, String role, Boolean active, Pageable pageable) {
        return adminUserJpaRepo.findAllWithFilters(keyword, role, active, pageable)
                .map(UserModel::toDomain);
    }
}
