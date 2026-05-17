package com.example.ondas_be.domain.repoport;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SongTagRepoPort {

    void replaceSongTags(UUID songId, List<Long> tagIds);

    List<Long> findTagIdsBySongId(UUID songId);

    Map<UUID, List<Long>> findTagIdsBySongIds(Collection<UUID> songIds);
}
