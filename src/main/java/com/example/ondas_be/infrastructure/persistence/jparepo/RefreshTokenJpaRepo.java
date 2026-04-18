package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.RefreshTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepo extends JpaRepository<RefreshTokenModel, UUID> {

    Optional<RefreshTokenModel> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshTokenModel rt set rt.revoked = true where rt.userId = :userId and rt.revoked = false")
    void revokeAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("update RefreshTokenModel rt set rt.revoked = true where rt.tokenHash = :tokenHash")
    void revokeByTokenHash(@Param("tokenHash") String tokenHash);
}