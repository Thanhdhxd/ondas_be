package com.example.ondas_be.domain.repoport;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SongArtistRepoPort {

    void replaceSongArtists(UUID songId, List<UUID> artistIds);

    List<UUID> findArtistIdsBySongId(UUID songId);

    Map<UUID, List<UUID>> findArtistIdsBySongIds(Collection<UUID> songIds);
}
