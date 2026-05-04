package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.Favorite;

import java.util.List;
import java.util.UUID;

public interface FavoriteRepoPort {

    void add(UUID userId, UUID songId);

    void remove(UUID userId, UUID songId);

    boolean exists(UUID userId, UUID songId);

    List<Favorite> findByUserId(UUID userId, int page, int size);

    long countByUserId(UUID userId);
}
