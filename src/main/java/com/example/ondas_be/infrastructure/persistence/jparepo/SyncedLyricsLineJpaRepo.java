package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.SyncedLyricsLineModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link SyncedLyricsLineModel}.
 */
public interface SyncedLyricsLineJpaRepo extends JpaRepository<SyncedLyricsLineModel, Integer> {

    List<SyncedLyricsLineModel> findByLyricsIdOrderByLineIndexAsc(UUID lyricsId);

    @Modifying
    @Query("DELETE FROM SyncedLyricsLineModel s WHERE s.lyricsId = :lyricsId")
    void deleteByLyricsId(@Param("lyricsId") UUID lyricsId);
}
