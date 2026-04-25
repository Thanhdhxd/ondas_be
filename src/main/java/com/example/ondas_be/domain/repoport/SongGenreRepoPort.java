package com.example.ondas_be.domain.repoport;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SongGenreRepoPort {

    void replaceSongGenres(UUID songId, List<Long> genreIds);

    List<Long> findGenreIdsBySongId(UUID songId);

    Map<UUID, List<Long>> findGenreIdsBySongIds(Collection<UUID> songIds);
}
