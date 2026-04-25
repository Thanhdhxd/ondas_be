package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.CreateSongRequest;
import com.example.ondas_be.application.dto.request.SongFilterRequest;
import com.example.ondas_be.application.dto.request.UpdateSongRequest;
import com.example.ondas_be.application.dto.response.ArtistSummaryResponse;
import com.example.ondas_be.application.dto.response.GenreSummaryResponse;
import com.example.ondas_be.application.dto.response.SongResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.exception.AlbumNotFoundException;
import com.example.ondas_be.application.exception.ArtistNotFoundException;
import com.example.ondas_be.application.exception.GenreNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.exception.StorageOperationException;
import com.example.ondas_be.application.mapper.ArtistMapper;
import com.example.ondas_be.application.mapper.GenreMapper;
import com.example.ondas_be.application.mapper.SongMapper;
import com.example.ondas_be.application.service.port.SongServicePort;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.application.util.SlugUtil;
import com.example.ondas_be.domain.entity.Artist;
import com.example.ondas_be.domain.entity.Genre;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.repoport.AlbumRepoPort;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.domain.repoport.GenreRepoPort;
import com.example.ondas_be.domain.repoport.SongArtistRepoPort;
import com.example.ondas_be.domain.repoport.SongGenreRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SongService implements SongServicePort {

    private final SongRepoPort songRepoPort;
    private final ArtistRepoPort artistRepoPort;
    private final AlbumRepoPort albumRepoPort;
    private final GenreRepoPort genreRepoPort;
    private final SongArtistRepoPort songArtistRepoPort;
    private final SongGenreRepoPort songGenreRepoPort;
    private final StoragePort storagePort;
    private final SongMapper songMapper;
    private final ArtistMapper artistMapper;
    private final GenreMapper genreMapper;

    @Value("${storage.minio.bucket-audio}")
    private String audioBucket;

    @Value("${storage.minio.bucket-image}")
    private String imageBucket;

    @Override
    @Transactional
    public SongResponse createSong(CreateSongRequest request, MultipartFile audioFile, MultipartFile coverFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("Audio file is required");
        }

        validateArtists(request.getArtistIds());
        validateGenres(request.getGenreIds());
        validateAlbum(request.getAlbumId());

        String slug = resolveUniqueSlug(SlugUtil.toSlug(request.getTitle()), null);
        String audioObjectName = buildObjectName("songs/audio/", audioFile.getOriginalFilename());
        String audioUrl = uploadFile(audioBucket, audioObjectName, audioFile);
        String coverUrl = uploadOptionalImage(coverFile, "songs/cover/");

        Song song = new Song(
                null,
                request.getTitle().trim(),
                slug,
                extractDurationSeconds(audioFile),
                audioUrl,
                resolveAudioFormat(audioFile.getOriginalFilename()),
                audioFile.getSize(),
                coverUrl,
                request.getAlbumId(),
                request.getTrackNumber(),
                request.getReleaseDate(),
                0L,
                true,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                request.getArtistIds(),
                request.getGenreIds()
        );

        Song savedSong = songRepoPort.save(song);
        songArtistRepoPort.replaceSongArtists(savedSong.getId(), request.getArtistIds());
        songGenreRepoPort.replaceSongGenres(savedSong.getId(), request.getGenreIds());

        return buildResponse(savedSong, request.getArtistIds(), request.getGenreIds());
    }

    @Override
    @Transactional
    public SongResponse updateSong(UUID id, UpdateSongRequest request, MultipartFile audioFile, MultipartFile coverFile) {
        Song existing = songRepoPort.findById(id)
                .orElseThrow(() -> new SongNotFoundException("Song not found with id: " + id));

        if (request.getArtistIds() != null) {
            validateArtists(request.getArtistIds());
        }
        if (request.getGenreIds() != null) {
            validateGenres(request.getGenreIds());
        }
        if (request.getAlbumId() != null) {
            validateAlbum(request.getAlbumId());
        }

        String title = request.getTitle() != null ? request.getTitle().trim() : existing.getTitle();
        String slug = existing.getSlug();
        if (request.getTitle() != null && !Objects.equals(existing.getTitle(), title)) {
            slug = resolveUniqueSlug(SlugUtil.toSlug(title), existing.getSlug());
        }

        String audioUrl = existing.getAudioUrl();
        String audioFormat = existing.getAudioFormat();
        Long audioSize = existing.getAudioSizeBytes();
        if (audioFile != null && !audioFile.isEmpty()) {
            String audioObjectName = buildObjectName("songs/audio/", audioFile.getOriginalFilename());
            audioUrl = uploadFile(audioBucket, audioObjectName, audioFile);
            audioFormat = resolveAudioFormat(audioFile.getOriginalFilename());
            audioSize = audioFile.getSize();
            deleteObject(audioBucket, existing.getAudioUrl());
        }

        String coverUrl = existing.getCoverUrl();
        if (coverFile != null && !coverFile.isEmpty()) {
            coverUrl = uploadOptionalImage(coverFile, "songs/cover/");
            deleteObject(imageBucket, existing.getCoverUrl());
        }

        UUID albumId = request.getAlbumId() != null ? request.getAlbumId() : existing.getAlbumId();
        Integer trackNumber = request.getTrackNumber() != null ? request.getTrackNumber() : existing.getTrackNumber();
        Integer durationSeconds = (audioFile != null && !audioFile.isEmpty())
                ? extractDurationSeconds(audioFile)
                : existing.getDurationSeconds();

        Song updatedSong = new Song(
                existing.getId(),
                title,
                slug,
                durationSeconds,
                audioUrl,
                audioFormat,
                audioSize,
                coverUrl,
                albumId,
                trackNumber,
                request.getReleaseDate() != null ? request.getReleaseDate() : existing.getReleaseDate(),
                existing.getPlayCount(),
                request.getActive() != null ? request.getActive() : existing.isActive(),
                existing.getCreatedBy(),
                existing.getCreatedAt(),
                existing.getUpdatedAt(),
                existing.getArtistIds(),
                existing.getGenreIds()
        );

        Song savedSong = songRepoPort.save(updatedSong);

        List<UUID> artistIds = request.getArtistIds() != null ? request.getArtistIds()
                : songArtistRepoPort.findArtistIdsBySongId(savedSong.getId());
        List<Long> genreIds = request.getGenreIds() != null ? request.getGenreIds()
                : songGenreRepoPort.findGenreIdsBySongId(savedSong.getId());

        if (request.getArtistIds() != null) {
            songArtistRepoPort.replaceSongArtists(savedSong.getId(), request.getArtistIds());
        }
        if (request.getGenreIds() != null) {
            songGenreRepoPort.replaceSongGenres(savedSong.getId(), request.getGenreIds());
        }

        return buildResponse(savedSong, artistIds, genreIds);
    }

    @Override
    @Transactional(readOnly = true)
    public SongResponse getSongById(UUID id) {
        Song song = songRepoPort.findById(id)
                .orElseThrow(() -> new SongNotFoundException("Song not found with id: " + id));

        List<UUID> artistIds = songArtistRepoPort.findArtistIdsBySongId(id);
        List<Long> genreIds = songGenreRepoPort.findGenreIdsBySongId(id);

        return buildResponse(song, artistIds, genreIds);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<SongResponse> getSongs(SongFilterRequest filter) {
        int page = filter.getPage();
        int size = filter.getSize();
        String normalizedMode = filter.getMode() == null ? "contains" : filter.getMode().trim().toLowerCase();

        List<Song> songs;
        long total;

        if (filter.getQuery() != null && !filter.getQuery().isBlank()) {
            if ("fulltext".equals(normalizedMode)) {
                songs = songRepoPort.findByTitleFullText(filter.getQuery(), page, size);
                total = songRepoPort.countByTitleFullText(filter.getQuery());
            } else {
                songs = songRepoPort.findByTitleContains(filter.getQuery(), page, size);
                total = songRepoPort.countByTitleContains(filter.getQuery());
            }
        } else if (filter.getArtistId() != null) {
            if (!artistRepoPort.existsById(filter.getArtistId())) {
                throw new ArtistNotFoundException("Artist not found with id: " + filter.getArtistId());
            }
            songs = songRepoPort.findByArtistId(filter.getArtistId(), page, size);
            total = songRepoPort.countByArtistId(filter.getArtistId());
        } else if (filter.getAlbumId() != null) {
            if (!albumRepoPort.existsById(filter.getAlbumId())) {
                throw new AlbumNotFoundException("Album not found with id: " + filter.getAlbumId());
            }
            songs = songRepoPort.findByAlbumId(filter.getAlbumId(), page, size);
            total = songRepoPort.countByAlbumId(filter.getAlbumId());
        } else if (filter.getGenreId() != null) {
            if (!genreRepoPort.existsById(filter.getGenreId())) {
                throw new GenreNotFoundException("Genre not found with id: " + filter.getGenreId());
            }
            songs = songRepoPort.findByGenreId(filter.getGenreId(), page, size);
            total = songRepoPort.countByGenreId(filter.getGenreId());
        } else {
            songs = songRepoPort.findAll(page, size);
            total = songRepoPort.countAll();
        }

        List<UUID> songIds = songs.stream().map(Song::getId).toList();
        Map<UUID, List<UUID>> artistIdsBySong = songIds.isEmpty()
                ? Collections.emptyMap()
                : songArtistRepoPort.findArtistIdsBySongIds(songIds);
        Map<UUID, List<Long>> genreIdsBySong = songIds.isEmpty()
                ? Collections.emptyMap()
                : songGenreRepoPort.findGenreIdsBySongIds(songIds);

        List<UUID> allArtistIds = artistIdsBySong.values().stream()
                .flatMap(List::stream).distinct().toList();
        List<Long> allGenreIds = genreIdsBySong.values().stream()
                .flatMap(List::stream).distinct().toList();

        Map<UUID, ArtistSummaryResponse> artistById = artistRepoPort.findByIds(allArtistIds).stream()
                .collect(Collectors.toMap(Artist::getId, artistMapper::toSummaryResponse));
        Map<Long, GenreSummaryResponse> genreById = genreRepoPort.findByIds(allGenreIds).stream()
                .collect(Collectors.toMap(Genre::getId, genreMapper::toSummaryResponse));

        List<SongResponse> items = songs.stream().map(song -> {
            SongResponse response = songMapper.toResponse(song);
            response.setArtists(artistIdsBySong.getOrDefault(song.getId(), Collections.emptyList())
                    .stream().map(artistById::get).filter(Objects::nonNull).toList());
            response.setGenres(genreIdsBySong.getOrDefault(song.getId(), Collections.emptyList())
                    .stream().map(genreById::get).filter(Objects::nonNull).toList());
            return response;
        }).toList();

        return buildPageResult(items, page, size, total);
    }

    @Override
    @Transactional
    public void deleteSong(UUID id) {
        Song song = songRepoPort.findById(id)
                .orElseThrow(() -> new SongNotFoundException("Song not found with id: " + id));

        deleteObject(audioBucket, song.getAudioUrl());
        deleteObject(imageBucket, song.getCoverUrl());
        songArtistRepoPort.replaceSongArtists(id, List.of());
        songGenreRepoPort.replaceSongGenres(id, List.of());
        songRepoPort.deleteById(id);
    }

    private void validateArtists(List<UUID> artistIds) {
        for (UUID artistId : artistIds) {
            if (!artistRepoPort.existsById(artistId)) {
                throw new ArtistNotFoundException("Artist not found with id: " + artistId);
            }
        }
    }

    private void validateGenres(List<Long> genreIds) {
        for (Long genreId : genreIds) {
            if (!genreRepoPort.existsById(genreId)) {
                throw new GenreNotFoundException("Genre not found with id: " + genreId);
            }
        }
    }

    private void validateAlbum(UUID albumId) {
        if (albumId != null && !albumRepoPort.existsById(albumId)) {
            throw new AlbumNotFoundException("Album not found with id: " + albumId);
        }
    }

    private String resolveUniqueSlug(String slugCandidate, String currentSlug) {
        if (slugCandidate == null) {
            return currentSlug;
        }
        if (currentSlug != null && currentSlug.equals(slugCandidate)) {
            return currentSlug;
        }
        if (!songRepoPort.existsBySlug(slugCandidate)) {
            return slugCandidate;
        }
        return slugCandidate + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private SongResponse buildResponse(Song song, List<UUID> artistIds, List<Long> genreIds) {
        SongResponse response = songMapper.toResponse(song);
        response.setArtists(fetchArtistSummaries(artistIds));
        response.setGenres(fetchGenreSummaries(genreIds));
        return response;
    }

    private List<ArtistSummaryResponse> fetchArtistSummaries(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return artistMapper.toSummaryResponseList(artistRepoPort.findByIds(ids));
    }

    private List<GenreSummaryResponse> fetchGenreSummaries(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return genreMapper.toSummaryResponseList(genreRepoPort.findByIds(ids));
    }

    private String buildObjectName(String prefix, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }
        return prefix + UUID.randomUUID() + extension;
    }

    private String uploadOptionalImage(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String objectName = buildObjectName(prefix, file.getOriginalFilename());
        return uploadFile(imageBucket, objectName, file);
    }

    private String uploadFile(String bucket, String objectName, MultipartFile file) {
        try {
            return storagePort.upload(bucket, objectName, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException ex) {
            throw new StorageOperationException("Cannot read upload stream", ex);
        }
    }

    private void deleteObject(String bucket, String url) {
        String objectName = storagePort.extractObjectName(bucket, url);
        storagePort.delete(bucket, objectName);
    }

    private String resolveAudioFormat(String originalFilename) {
        if (originalFilename == null) {
            return "mp3";
        }
        int idx = originalFilename.lastIndexOf('.');
        if (idx < 0 || idx == originalFilename.length() - 1) {
            return "mp3";
        }
        return originalFilename.substring(idx + 1).toLowerCase();
    }

    private Integer extractDurationSeconds(MultipartFile audioFile) {
        File tempFile = null;
        try {
            String suffix = "." + resolveAudioFormat(audioFile.getOriginalFilename());
            tempFile = File.createTempFile("audio-upload-", suffix);
            audioFile.transferTo(tempFile);
            AudioFile af = AudioFileIO.read(tempFile);
            return af.getAudioHeader().getTrackLength();
        } catch (Exception e) {
            log.warn("Cannot extract duration from audio file '{}': {}", audioFile.getOriginalFilename(), e.getMessage());
            return null;
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    private PageResultDto<SongResponse> buildPageResult(List<SongResponse> items, int page, int size, long total) {
        int safeSize = Math.max(1, size);
        int totalPages = (int) Math.ceil((double) total / safeSize);
        return PageResultDto.<SongResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }
}
