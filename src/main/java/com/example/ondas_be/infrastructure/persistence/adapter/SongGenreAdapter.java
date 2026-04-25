package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.repoport.SongGenreRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.SongGenreJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.SongGenreId;
import com.example.ondas_be.infrastructure.persistence.model.SongGenreModel;
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
public class SongGenreAdapter implements SongGenreRepoPort {

    private final SongGenreJpaRepo songGenreJpaRepo;

    @Override
    public void replaceSongGenres(UUID songId, List<Long> genreIds) {
        songGenreJpaRepo.deleteByIdSongId(songId);
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }
        List<SongGenreModel> models = genreIds.stream()
                .map(genreId -> SongGenreModel.builder()
                        .id(new SongGenreId(songId, genreId))
                        .build())
                .toList();
        songGenreJpaRepo.saveAll(models);
    }

    @Override
    public List<Long> findGenreIdsBySongId(UUID songId) {
        List<SongGenreModel> models = songGenreJpaRepo.findByIdSongId(songId);
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(model -> model.getId().getGenreId()).toList();
    }

    @Override
    public Map<UUID, List<Long>> findGenreIdsBySongIds(Collection<UUID> songIds) {
        if (songIds == null || songIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return songGenreJpaRepo.findByIdSongIdIn(songIds).stream()
                .collect(Collectors.groupingBy(
                        model -> model.getId().getSongId(),
                        Collectors.mapping(model -> model.getId().getGenreId(), Collectors.toList())
                ));
    }
}
