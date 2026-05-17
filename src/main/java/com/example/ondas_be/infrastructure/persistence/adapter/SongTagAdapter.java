package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.repoport.SongTagRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.SongTagJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.SongTagId;
import com.example.ondas_be.infrastructure.persistence.model.SongTagModel;
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
public class SongTagAdapter implements SongTagRepoPort {

    private final SongTagJpaRepo songTagJpaRepo;

    @Override
    public void replaceSongTags(UUID songId, List<Long> tagIds) {
        songTagJpaRepo.deleteByIdSongId(songId);
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<SongTagModel> models = tagIds.stream()
                .map(tagId -> SongTagModel.builder()
                        .id(new SongTagId(songId, tagId))
                        .build())
                .toList();
        songTagJpaRepo.saveAll(models);
    }

    @Override
    public List<Long> findTagIdsBySongId(UUID songId) {
        List<SongTagModel> models = songTagJpaRepo.findByIdSongId(songId);
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        return models.stream().map(model -> model.getId().getTagId()).toList();
    }

    @Override
    public Map<UUID, List<Long>> findTagIdsBySongIds(Collection<UUID> songIds) {
        if (songIds == null || songIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return songTagJpaRepo.findByIdSongIdIn(songIds).stream()
                .collect(Collectors.groupingBy(
                        model -> model.getId().getSongId(),
                        Collectors.mapping(model -> model.getId().getTagId(), Collectors.toList())
                ));
    }
}
