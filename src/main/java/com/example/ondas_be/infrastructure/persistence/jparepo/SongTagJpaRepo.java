package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.SongTagId;
import com.example.ondas_be.infrastructure.persistence.model.SongTagModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SongTagJpaRepo extends JpaRepository<SongTagModel, SongTagId> {

    void deleteByIdSongId(UUID songId);

    List<SongTagModel> findByIdSongId(UUID songId);

    List<SongTagModel> findByIdSongIdIn(Collection<UUID> songIds);
}
