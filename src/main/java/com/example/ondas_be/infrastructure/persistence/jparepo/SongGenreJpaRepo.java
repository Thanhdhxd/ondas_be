package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.SongGenreId;
import com.example.ondas_be.infrastructure.persistence.model.SongGenreModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SongGenreJpaRepo extends JpaRepository<SongGenreModel, SongGenreId> {

    void deleteByIdSongId(UUID songId);

    List<SongGenreModel> findByIdSongId(UUID songId);

    List<SongGenreModel> findByIdSongIdIn(Collection<UUID> songIds);
}
