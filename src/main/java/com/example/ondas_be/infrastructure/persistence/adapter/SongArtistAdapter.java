package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.repoport.SongArtistRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.SongArtistJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.SongArtistId;
import com.example.ondas_be.infrastructure.persistence.model.SongArtistModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SongArtistAdapter implements SongArtistRepoPort {

    private final SongArtistJpaRepo songArtistJpaRepo;

    @Override
    public void replaceSongArtists(UUID songId, List<UUID> artistIds) {
        songArtistJpaRepo.deleteByIdSongId(songId);
        if (artistIds == null || artistIds.isEmpty()) {
            return;
        }
        List<SongArtistModel> models = artistIds.stream()
                .map(artistId -> SongArtistModel.builder()
                        .id(new SongArtistId(songId, artistId))
                        .role("main")
                        .build())
                .toList();
        songArtistJpaRepo.saveAll(models);
    }

    @Override
    public List<UUID> findArtistIdsBySongId(UUID songId) {
        List<SongArtistModel> models = songArtistJpaRepo.findByIdSongId(songId);
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(model -> model.getId().getArtistId()).toList();
    }

    @Override
    public Map<UUID, List<UUID>> findArtistIdsBySongIds(Collection<UUID> songIds) {
        if (songIds == null || songIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return songArtistJpaRepo.findByIdSongIdIn(songIds).stream()
                .collect(Collectors.groupingBy(
                        model -> model.getId().getSongId(),
                        Collectors.mapping(model -> model.getId().getArtistId(), Collectors.toList())
                ));
    }
}
