package com.example.ondas_be.domain.repoport;

import java.util.List;
import java.util.UUID;

public interface AlbumArtistRepoPort {

    void replaceAlbumArtists(UUID albumId, List<UUID> artistIds);

    List<UUID> findArtistIdsByAlbumId(UUID albumId);

    List<UUID> findAlbumIdsByArtistId(UUID artistId);
}
