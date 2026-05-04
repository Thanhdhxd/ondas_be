package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.FavoriteId;
import com.example.ondas_be.infrastructure.persistence.model.FavoriteModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FavoriteJpaRepo extends JpaRepository<FavoriteModel, FavoriteId> {

    Page<FavoriteModel> findByIdUserId(UUID userId, Pageable pageable);

    long countByIdUserId(UUID userId);
}
