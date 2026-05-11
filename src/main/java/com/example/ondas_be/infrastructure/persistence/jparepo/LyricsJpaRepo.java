package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.LyricsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link LyricsModel}.
 */
public interface LyricsJpaRepo extends JpaRepository<LyricsModel, UUID> {

    Optional<LyricsModel> findBySongId(UUID songId);

    @Modifying
    @Query("UPDATE LyricsModel l SET l.plainText = :plainText, l.language = :language WHERE l.id = :id")
    void updateStaticLyrics(@Param("id") UUID id,
                            @Param("plainText") String plainText,
                            @Param("language") String language);

    @Modifying
    @Query("UPDATE LyricsModel l SET l.hasSynced = :hasSynced WHERE l.id = :id")
    void updateHasSynced(@Param("id") UUID id, @Param("hasSynced") boolean hasSynced);
}
