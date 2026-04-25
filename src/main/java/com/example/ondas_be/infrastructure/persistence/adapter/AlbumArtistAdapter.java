package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.repoport.AlbumArtistRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.AlbumArtistJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.AlbumArtistId;
import com.example.ondas_be.infrastructure.persistence.model.AlbumArtistModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlbumArtistAdapter implements AlbumArtistRepoPort {

    private final AlbumArtistJpaRepo albumArtistJpaRepo;

    @Override
    public void replaceAlbumArtists(UUID albumId, List<UUID> artistIds) {
        albumArtistJpaRepo.deleteByIdAlbumId(albumId);
        if (artistIds == null || artistIds.isEmpty()) {
            return;
        }
        List<AlbumArtistModel> models = buildModels(albumId, artistIds);
        albumArtistJpaRepo.saveAll(models);
    }

    @Override
    public List<UUID> findArtistIdsByAlbumId(UUID albumId) {
        List<AlbumArtistModel> models = albumArtistJpaRepo.findByIdAlbumId(albumId);
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(model -> model.getId().getArtistId()).toList();
    }

    @Override
    public List<UUID> findAlbumIdsByArtistId(UUID artistId) {
        List<AlbumArtistModel> models = albumArtistJpaRepo.findByIdArtistId(artistId);
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(model -> model.getId().getAlbumId()).toList();
    }

    private List<AlbumArtistModel> buildModels(UUID albumId, List<UUID> artistIds) {
        return artistIds.stream()
                .map(artistId -> AlbumArtistModel.builder()
                        .id(new AlbumArtistId(albumId, artistId))
                        .primary(artistIds.indexOf(artistId) == 0)
                        .build())
                .toList();
    }
}
