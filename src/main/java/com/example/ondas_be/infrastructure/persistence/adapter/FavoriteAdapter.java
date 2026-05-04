package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.Favorite;
import com.example.ondas_be.domain.repoport.FavoriteRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.FavoriteJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.FavoriteId;
import com.example.ondas_be.infrastructure.persistence.model.FavoriteModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FavoriteAdapter implements FavoriteRepoPort {

    private final FavoriteJpaRepo favoriteJpaRepo;

    @Override
    public void add(UUID userId, UUID songId) {
        favoriteJpaRepo.save(FavoriteModel.fromDomain(userId, songId));
    }

    @Override
    public void remove(UUID userId, UUID songId) {
        favoriteJpaRepo.deleteById(new FavoriteId(userId, songId));
    }

    @Override
    public boolean exists(UUID userId, UUID songId) {
        return favoriteJpaRepo.existsById(new FavoriteId(userId, songId));
    }

    @Override
    public List<Favorite> findByUserId(UUID userId, int page, int size) {
        return favoriteJpaRepo
                .findByIdUserId(userId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(FavoriteModel::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(UUID userId) {
        return favoriteJpaRepo.countByIdUserId(userId);
    }
}
