package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.CreateAlbumRequest;
import com.example.ondas_be.application.dto.request.UpdateAlbumRequest;
import com.example.ondas_be.application.dto.response.AlbumResponse;
import com.example.ondas_be.application.dto.response.SongSummaryResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.exception.AlbumNotFoundException;
import com.example.ondas_be.application.exception.ArtistNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.exception.StorageOperationException;
import com.example.ondas_be.application.mapper.AlbumMapper;
import com.example.ondas_be.application.mapper.SongMapper;
import com.example.ondas_be.application.service.port.AlbumServicePort;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.application.util.SlugUtil;
import com.example.ondas_be.domain.entity.Album;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.repoport.AlbumArtistRepoPort;
import com.example.ondas_be.domain.repoport.AlbumRepoPort;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumService implements AlbumServicePort {

    private final AlbumRepoPort albumRepoPort;
    private final ArtistRepoPort artistRepoPort;
    private final AlbumArtistRepoPort albumArtistRepoPort;
    private final SongRepoPort songRepoPort;
    private final StoragePort storagePort;
    private final AlbumMapper albumMapper;
    private final SongMapper songMapper;

    @Value("${storage.minio.bucket-image}")
    private String imageBucket;

    @Override
    @Transactional
    public AlbumResponse createAlbum(CreateAlbumRequest request, MultipartFile coverFile) {
        validateArtists(request.getArtistIds());
        String slug = resolveUniqueSlug(resolveSlug(request.getSlug(), request.getTitle()), null);
        String coverUrl = uploadOptionalImage(coverFile, "albums/cover/");

        Album album = new Album(
                null,
                request.getTitle().trim(),
                slug,
                coverUrl,
                request.getReleaseDate(),
                resolveAlbumType(request.getAlbumType()),
                request.getDescription(),
                0,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                request.getArtistIds()
        );

        Album saved = albumRepoPort.save(album);
        albumArtistRepoPort.replaceAlbumArtists(saved.getId(), request.getArtistIds());
        replaceSongsForAlbum(saved.getId(), request.getSongIds());
        saved = refreshAlbumTrackCount(saved.getId());

        AlbumResponse response = albumMapper.toResponse(saved);
        response.setArtistIds(request.getArtistIds());
        response.setTracklist(buildTracklist(saved.getId()));
        return response;
    }

    @Override
    @Transactional
    public AlbumResponse updateAlbum(UUID id, UpdateAlbumRequest request, MultipartFile coverFile) {
        Album existing = albumRepoPort.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + id));

        if (request.getArtistIds() != null) {
            validateArtists(request.getArtistIds());
        }

        String title = request.getTitle() != null ? request.getTitle().trim() : existing.getTitle();
        String slug = existing.getSlug();
        if (request.getTitle() != null || request.getSlug() != null) {
            String slugCandidate = resolveSlug(request.getSlug(), title);
            if (!slugCandidate.equals(existing.getSlug())) {
                slug = resolveUniqueSlug(slugCandidate, existing.getSlug());
            }
        }

        String coverUrl = existing.getCoverUrl();
        if (coverFile != null && !coverFile.isEmpty()) {
            coverUrl = uploadOptionalImage(coverFile, "albums/cover/");
            deleteObject(existing.getCoverUrl());
        }

        Album updated = new Album(
                existing.getId(),
                title,
                slug,
                coverUrl,
                request.getReleaseDate() != null ? request.getReleaseDate() : existing.getReleaseDate(),
                resolveAlbumType(request.getAlbumType() != null ? request.getAlbumType() : existing.getAlbumType()),
                request.getDescription() != null ? request.getDescription() : existing.getDescription(),
                existing.getTotalTracks(),
                existing.getCreatedBy(),
                existing.getCreatedAt(),
                existing.getUpdatedAt(),
                existing.getArtistIds()
        );

        Album saved = albumRepoPort.save(updated);
        if (request.getArtistIds() != null) {
            albumArtistRepoPort.replaceAlbumArtists(saved.getId(), request.getArtistIds());
        }

        if (request.getSongIds() != null) {
            replaceSongsForAlbum(saved.getId(), request.getSongIds());
        }
        saved = refreshAlbumTrackCount(saved.getId());

        AlbumResponse response = albumMapper.toResponse(saved);
        response.setArtistIds(request.getArtistIds() != null ? request.getArtistIds()
                : albumArtistRepoPort.findArtistIdsByAlbumId(saved.getId()));
        response.setTracklist(buildTracklist(saved.getId()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AlbumResponse getAlbumById(UUID id) {
        Album album = albumRepoPort.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + id));

        AlbumResponse response = albumMapper.toResponse(album);
        response.setArtistIds(albumArtistRepoPort.findArtistIdsByAlbumId(id));
        response.setTracklist(buildTracklist(id));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlbumResponse> getAllAlbums() {
        return albumRepoPort.findAll().stream().map(album -> {
            AlbumResponse response = albumMapper.toResponse(album);
            response.setArtistIds(albumArtistRepoPort.findArtistIdsByAlbumId(album.getId()));
            response.setTracklist(List.of());
            return response;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<AlbumResponse> searchAlbumsByTitle(String query, String mode, int page, int size) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query is required");
        }
        String normalizedMode = mode == null ? "contains" : mode.trim().toLowerCase();
        List<Album> albums;
        long total;
        if ("fulltext".equals(normalizedMode)) {
            albums = albumRepoPort.findByTitleFullText(query, page, size);
            total = albumRepoPort.countByTitleFullText(query);
        } else {
            albums = albumRepoPort.findByTitleContains(query, page, size);
            total = albumRepoPort.countByTitleContains(query);
        }
        List<AlbumResponse> items = albums.stream().map(album -> {
            AlbumResponse response = albumMapper.toResponse(album);
            response.setArtistIds(albumArtistRepoPort.findArtistIdsByAlbumId(album.getId()));
            response.setTracklist(List.of());
            return response;
        }).toList();
        return buildPageResult(items, page, size, total);
    }

    @Override
    @Transactional(readOnly = true)
    public void deleteAlbum(UUID id) {
        Album album = albumRepoPort.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + id));

        deleteObject(album.getCoverUrl());
        albumArtistRepoPort.replaceAlbumArtists(id, List.of());
        albumRepoPort.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlbumResponse> getAlbumsByArtistId(UUID artistId) {
        if (!artistRepoPort.existsById(artistId)) {
            throw new ArtistNotFoundException("Artist not found with id: " + artistId);
        }
        List<UUID> albumIds = albumArtistRepoPort.findAlbumIdsByArtistId(artistId);
        return albumIds.stream()
                .map(albumRepoPort::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(album -> {
                    AlbumResponse response = albumMapper.toResponse(album);
                    response.setArtistIds(albumArtistRepoPort.findArtistIdsByAlbumId(album.getId()));
                    response.setTracklist(List.of());
                    return response;
                }).toList();
    }

    private List<SongSummaryResponse> buildTracklist(UUID albumId) {
        List<Song> songs = songRepoPort.findByAlbumIdOrderByTrackNumber(albumId);
        return songMapper.toSummaryResponseList(songs);
    }

    private void replaceSongsForAlbum(UUID albumId, List<UUID> songIds) {
        List<Song> currentSongs = songRepoPort.findByAlbumIdOrderByTrackNumber(albumId);
        Set<UUID> targetSongIds = songIds == null
                ? Set.of()
                : new LinkedHashSet<>(songIds);

        for (Song currentSong : currentSongs) {
            if (targetSongIds.contains(currentSong.getId())) {
                continue;
            }
            songRepoPort.save(copySongWithAlbum(currentSong, null, null));
        }

        List<UUID> orderedSongIds = new ArrayList<>(targetSongIds);
        for (int index = 0; index < orderedSongIds.size(); index++) {
            UUID songId = orderedSongIds.get(index);
            Song existingSong = songRepoPort.findById(songId)
                    .orElseThrow(() -> new SongNotFoundException("Song not found with id: " + songId));

            if (existingSong.getAlbumId() != null && !existingSong.getAlbumId().equals(albumId)) {
                throw new IllegalArgumentException("Song already belongs to another album: " + songId);
            }

                songRepoPort.save(copySongWithAlbum(existingSong, albumId, index + 1));
        }
    }

            private Song copySongWithAlbum(Song source, UUID albumId, Integer trackNumber) {
            return new Song(
                source.getId(),
                source.getTitle(),
                source.getSlug(),
                source.getDurationSeconds(),
                source.getAudioUrl(),
                source.getAudioFormat(),
                source.getAudioSizeBytes(),
                source.getCoverUrl(),
                albumId,
                trackNumber,
                source.getReleaseDate(),
                source.getPlayCount(),
                source.isActive(),
                source.getCreatedBy(),
                source.getCreatedAt(),
                LocalDateTime.now(),
                source.getArtistIds(),
                source.getGenreIds());
            }

            private Album refreshAlbumTrackCount(UUID albumId) {
            Album currentAlbum = albumRepoPort.findById(albumId)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + albumId));
            int totalTracks = songRepoPort.findByAlbumIdOrderByTrackNumber(albumId).size();

            Album updatedAlbum = new Album(
                currentAlbum.getId(),
                currentAlbum.getTitle(),
                currentAlbum.getSlug(),
                currentAlbum.getCoverUrl(),
                currentAlbum.getReleaseDate(),
                currentAlbum.getAlbumType(),
                currentAlbum.getDescription(),
                totalTracks,
                currentAlbum.getCreatedBy(),
                currentAlbum.getCreatedAt(),
                currentAlbum.getUpdatedAt(),
                currentAlbum.getArtistIds());

            return albumRepoPort.save(updatedAlbum);
            }

    private void validateArtists(List<UUID> artistIds) {
        for (UUID artistId : artistIds) {
            if (!artistRepoPort.existsById(artistId)) {
                throw new ArtistNotFoundException("Artist not found with id: " + artistId);
            }
        }
    }

    private String resolveSlug(String slug, String title) {
        if (slug != null && !slug.isBlank()) {
            return SlugUtil.toSlug(slug);
        }
        return SlugUtil.toSlug(title);
    }

    private String resolveUniqueSlug(String slugCandidate, String currentSlug) {
        if (slugCandidate == null) {
            return currentSlug;
        }
        if (currentSlug != null && currentSlug.equals(slugCandidate)) {
            return currentSlug;
        }
        if (!albumRepoPort.existsBySlug(slugCandidate)) {
            return slugCandidate;
        }
        return slugCandidate + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String resolveAlbumType(String albumType) {
        if (albumType == null || albumType.isBlank()) {
            return "album";
        }
        return albumType.toLowerCase();
    }

    private String uploadOptionalImage(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String objectName = prefix + UUID.randomUUID() + resolveExtension(file.getOriginalFilename());
        try {
            return storagePort.upload(imageBucket, objectName, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException ex) {
            throw new StorageOperationException("Cannot read upload stream", ex);
        }
    }

    private void deleteObject(String url) {
        String objectName = storagePort.extractObjectName(imageBucket, url);
        storagePort.delete(imageBucket, objectName);
    }

    private PageResultDto<AlbumResponse> buildPageResult(List<AlbumResponse> items, int page, int size, long total) {
        int safeSize = Math.max(1, size);
        int totalPages = (int) Math.ceil((double) total / safeSize);
        return PageResultDto.<AlbumResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
    }
}
