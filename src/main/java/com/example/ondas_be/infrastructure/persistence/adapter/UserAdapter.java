package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.UserJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAdapter implements UserRepoPort {

    private final UserJpaRepo userJpaRepo;

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepo.findById(id).map(UserModel::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepo.findByEmail(email).map(UserModel::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepo.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserModel model = UserModel.fromDomain(user);
        return userJpaRepo.save(model).toDomain();
    }
}
