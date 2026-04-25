package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.AlbumArtistId;
import com.example.ondas_be.infrastructure.persistence.model.AlbumArtistModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlbumArtistJpaRepo extends JpaRepository<AlbumArtistModel, AlbumArtistId> {

    void deleteByIdAlbumId(UUID albumId);

    List<AlbumArtistModel> findByIdAlbumId(UUID albumId);

    List<AlbumArtistModel> findByIdArtistId(UUID artistId);
}
