package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.OtpCodeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OtpCodeJpaRepo extends JpaRepository<OtpCodeModel, UUID> {

    Optional<OtpCodeModel> findByUserIdAndCodeHashAndUsedFalse(UUID userId, String codeHash);

    @Modifying
    @Query("update OtpCodeModel oc set oc.used = true where oc.userId = :userId and oc.used = false")
    void markAllUnusedByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("update OtpCodeModel oc set oc.used = true where oc.id = :id and oc.used = false")
    void markAsUsed(@Param("id") UUID id);
}
