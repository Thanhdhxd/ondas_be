package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepoPort {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User save(User user);
}
