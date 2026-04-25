package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.SongJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.SongModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SongAdapter implements SongRepoPort {

    private final SongJpaRepo songJpaRepo;

    @Override
    public Song save(Song song) {
        return songJpaRepo.save(SongModel.fromDomain(song)).toDomain();
    }

    @Override
    public Optional<Song> findById(UUID id) {
        return songJpaRepo.findById(id).map(SongModel::toDomain);
    }

    @Override
    public List<Song> findAll(int page, int size) {
        return songJpaRepo.findAll(PageRequest.of(page, size)).map(SongModel::toDomain).toList();
    }

    @Override
    public long countAll() {
        return songJpaRepo.count();
    }

    @Override
    public List<Song> findByAlbumIdOrderByTrackNumber(UUID albumId) {
        return songJpaRepo.findByAlbumIdOrderByTrackNumber(albumId).stream().map(SongModel::toDomain).toList();
    }

    @Override
    public List<Song> findByAlbumId(UUID albumId, int page, int size) {
        return songJpaRepo.findByAlbumId(albumId, PageRequest.of(page, size))
                .map(SongModel::toDomain)
                .toList();
    }

    @Override
    public long countByAlbumId(UUID albumId) {
        return songJpaRepo.countByAlbumId(albumId);
    }

    @Override
    public List<Song> findByArtistId(UUID artistId, int page, int size) {
        return songJpaRepo.findByArtistId(artistId, PageRequest.of(page, size))
                .map(SongModel::toDomain)
                .toList();
    }

    @Override
    public long countByArtistId(UUID artistId) {
        return songJpaRepo.countByArtistId(artistId);
    }

    @Override
    public List<Song> findByGenreId(Long genreId, int page, int size) {
        return songJpaRepo.findByGenreId(genreId, PageRequest.of(page, size))
                .map(SongModel::toDomain)
                .toList();
    }

    @Override
    public long countByGenreId(Long genreId) {
        return songJpaRepo.countByGenreId(genreId);
    }

    @Override
    public List<Song> findByTitleContains(String query, int page, int size) {
        return songJpaRepo.findByTitleContains(query, PageRequest.of(page, size))
                .map(SongModel::toDomain)
                .toList();
    }

    @Override
    public long countByTitleContains(String query) {
        return songJpaRepo.countByTitleContains(query);
    }

    @Override
    public List<Song> findByTitleFullText(String query, int page, int size) {
        return songJpaRepo.findByTitleFullText(query, PageRequest.of(page, size))
                .map(SongModel::toDomain)
                .toList();
    }

    @Override
    public long countByTitleFullText(String query) {
        return songJpaRepo.countByTitleFullText(query);
    }

    @Override
    public void deleteById(UUID id) {
        songJpaRepo.deleteById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return songJpaRepo.existsBySlug(slug);
    }
}
