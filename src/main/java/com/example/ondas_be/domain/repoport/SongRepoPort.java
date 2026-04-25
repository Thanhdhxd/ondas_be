package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.Song;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SongRepoPort {

    Song save(Song song);

    Optional<Song> findById(UUID id);

    List<Song> findAll(int page, int size);

    long countAll();

    List<Song> findByAlbumIdOrderByTrackNumber(UUID albumId);

    List<Song> findByAlbumId(UUID albumId, int page, int size);

    long countByAlbumId(UUID albumId);

    List<Song> findByArtistId(UUID artistId, int page, int size);

    long countByArtistId(UUID artistId);

    List<Song> findByGenreId(Long genreId, int page, int size);

    long countByGenreId(Long genreId);

    List<Song> findByTitleContains(String query, int page, int size);

    long countByTitleContains(String query);

    List<Song> findByTitleFullText(String query, int page, int size);

    long countByTitleFullText(String query);

    void deleteById(UUID id);

    boolean existsBySlug(String slug);
}
