package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.SongArtistId;
import com.example.ondas_be.infrastructure.persistence.model.SongArtistModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SongArtistJpaRepo extends JpaRepository<SongArtistModel, SongArtistId> {

    void deleteByIdSongId(UUID songId);

    List<SongArtistModel> findByIdSongId(UUID songId);

    List<SongArtistModel> findByIdSongIdIn(Collection<UUID> songIds);
}
