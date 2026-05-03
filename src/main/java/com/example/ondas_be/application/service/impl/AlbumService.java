package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.CreateAlbumRequest;
import com.example.ondas_be.application.dto.request.AlbumFilterRequest;
import com.example.ondas_be.application.dto.request.UpdateAlbumRequest;
import com.example.ondas_be.application.dto.response.AlbumResponse;
import com.example.ondas_be.application.dto.response.SongSummaryResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.exception.AlbumNotFoundException;
import com.example.ondas_be.application.exception.ArtistNotFoundException;
import com.example.ondas_be.application.exception.StorageOperationException;
import com.example.ondas_be.application.dto.response.ArtistSummaryResponse;
import com.example.ondas_be.application.mapper.AlbumMapper;
import com.example.ondas_be.application.mapper.ArtistMapper;
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
import java.util.List;
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
    private final ArtistMapper artistMapper;

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

        AlbumResponse response = albumMapper.toResponse(saved);
        response.setArtistIds(request.getArtistIds());
        response.setArtists(buildArtists(request.getArtistIds()));
        response.setTracklist(List.of());
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

        List<UUID> finalArtistIds = request.getArtistIds() != null
                ? request.getArtistIds()
                : albumArtistRepoPort.findArtistIdsByAlbumId(saved.getId());
        AlbumResponse response = albumMapper.toResponse(saved);
        response.setArtistIds(finalArtistIds);
        response.setArtists(buildArtists(finalArtistIds));
        response.setTracklist(buildTracklist(saved.getId()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AlbumResponse getAlbumById(UUID id) {
        Album album = albumRepoPort.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + id));

        List<UUID> artistIds = albumArtistRepoPort.findArtistIdsByAlbumId(id);
        long songCount = songRepoPort.countByAlbumId(id);
        AlbumResponse response = albumMapper.toResponse(album);
        response.setTotalTracks((int) songCount);
        response.setArtistIds(artistIds);
        response.setArtists(buildArtists(artistIds));
        response.setTracklist(buildTracklist(id));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<AlbumResponse> getAlbums(AlbumFilterRequest filter) {
        int page = filter.getPage();
        int size = filter.getSize();
        String normalizedMode = filter.getMode() == null ? "contains" : filter.getMode().trim().toLowerCase();

        List<Album> albums;
        long total;

        if (filter.getQuery() != null && !filter.getQuery().isBlank()) {
            if ("fulltext".equals(normalizedMode)) {
                albums = albumRepoPort.findByTitleFullText(filter.getQuery(), page, size);
                total = albumRepoPort.countByTitleFullText(filter.getQuery());
            } else {
                albums = albumRepoPort.findByTitleContains(filter.getQuery(), page, size);
                total = albumRepoPort.countByTitleContains(filter.getQuery());
            }
        } else {
            albums = albumRepoPort.findAll(page, size);
            total = albumRepoPort.countAll();
        }

        List<AlbumResponse> items = albums.stream().map(album -> {
            List<UUID> artistIds = albumArtistRepoPort.findArtistIdsByAlbumId(album.getId());
            long songCount = songRepoPort.countByAlbumId(album.getId());
            AlbumResponse response = albumMapper.toResponse(album);
            response.setTotalTracks((int) songCount); // lấy từ DB thực tế
            response.setArtistIds(artistIds);
            response.setArtists(buildArtists(artistIds));
            response.setTracklist(List.of());
            return response;
        }).toList();
        return buildPageResult(items, page, size, total);
    }

    @Override
    @Transactional
    public void deleteAlbum(UUID id) {
        Album album = albumRepoPort.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Album not found with id: " + id));

        deleteObject(album.getCoverUrl());
        albumArtistRepoPort.replaceAlbumArtists(id, List.of());
        albumRepoPort.deleteById(id);
    }

    private List<SongSummaryResponse> buildTracklist(UUID albumId) {
        List<Song> songs = songRepoPort.findByAlbumIdOrderByTrackNumber(albumId);
        return songMapper.toSummaryResponseList(songs);
    }

    /**
     * Đồng bộ lại albums.total_tracks bằng cách đếm song thực tế trong DB.
     */
    // private void refreshTotalTracks(Album album) {
    //     int count = (int) songRepoPort.countByAlbumId(album.getId());
    //     Album refreshed = new Album(
    //             album.getId(), album.getTitle(), album.getSlug(), album.getCoverUrl(),
    //             album.getReleaseDate(), album.getAlbumType(), album.getDescription(),
    //             count, album.getCreatedBy(), album.getCreatedAt(), album.getUpdatedAt(),
    //             album.getArtistIds()
    //     );
    //     albumRepoPort.save(refreshed);
    // }

    private List<ArtistSummaryResponse> buildArtists(List<UUID> artistIds) {
        if (artistIds == null || artistIds.isEmpty()) {
            return List.of();
        }
        return artistRepoPort.findByIds(artistIds).stream()
                .map(artistMapper::toSummaryResponse)
                .toList();
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
