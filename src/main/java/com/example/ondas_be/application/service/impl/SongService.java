package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.CreateSongRequest;
import com.example.ondas_be.application.dto.request.UpdateSongRequest;
import com.example.ondas_be.application.dto.response.SongResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.exception.AlbumNotFoundException;
import com.example.ondas_be.application.exception.ArtistNotFoundException;
import com.example.ondas_be.application.exception.GenreNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.exception.StorageOperationException;
import com.example.ondas_be.application.mapper.SongMapper;
import com.example.ondas_be.application.service.port.SongServicePort;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.application.util.SlugUtil;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.repoport.AlbumRepoPort;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.domain.repoport.GenreRepoPort;
import com.example.ondas_be.domain.repoport.SongArtistRepoPort;
import com.example.ondas_be.domain.repoport.SongGenreRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import lombok.RequiredArgsConstructor;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
        Integer durationSeconds = resolveDurationSeconds(
            request.getDurationSeconds(),
            audioFile,
            null
        );

        Song song = new Song(
                null,
                request.getTitle().trim(),
                slug,
            durationSeconds,
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

        return songMapper.toResponse(withRelations(savedSong, request.getArtistIds(), request.getGenreIds()));
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
        Integer durationSeconds = resolveDurationSeconds(
            request.getDurationSeconds(),
            audioFile,
            existing.getDurationSeconds()
        );

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

        return songMapper.toResponse(withRelations(savedSong, artistIds, genreIds));
    }

    @Override
    @Transactional(readOnly = true)
    public SongResponse getSongById(UUID id) {
        Song song = songRepoPort.findById(id)
                .orElseThrow(() -> new SongNotFoundException("Song not found with id: " + id));

        List<UUID> artistIds = songArtistRepoPort.findArtistIdsBySongId(id);
        List<Long> genreIds = songGenreRepoPort.findGenreIdsBySongId(id);

        return songMapper.toResponse(withRelations(song, artistIds, genreIds));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SongResponse> getAllSongs() {
        return songRepoPort.findAll().stream()
                .map(song -> songMapper.toResponse(
                        withRelations(song,
                                songArtistRepoPort.findArtistIdsBySongId(song.getId()),
                                songGenreRepoPort.findGenreIdsBySongId(song.getId()))))
                .toList();
    }

        @Override
        @Transactional(readOnly = true)
        public PageResultDto<SongResponse> getSongsByArtist(UUID artistId, int page, int size) {
        if (!artistRepoPort.existsById(artistId)) {
            throw new ArtistNotFoundException("Artist not found with id: " + artistId);
        }
        List<SongResponse> items = songRepoPort.findByArtistId(artistId, page, size).stream()
            .map(song -> songMapper.toResponse(withRelations(
                song,
                songArtistRepoPort.findArtistIdsBySongId(song.getId()),
                songGenreRepoPort.findGenreIdsBySongId(song.getId()))))
            .toList();
        long total = songRepoPort.countByArtistId(artistId);
        return buildPageResult(items, page, size, total);
        }

        @Override
        @Transactional(readOnly = true)
        public PageResultDto<SongResponse> getSongsByAlbum(UUID albumId, int page, int size) {
        if (albumId != null && !albumRepoPort.existsById(albumId)) {
            throw new AlbumNotFoundException("Album not found with id: " + albumId);
        }
        List<SongResponse> items = songRepoPort.findByAlbumId(albumId, page, size).stream()
            .map(song -> songMapper.toResponse(withRelations(
                song,
                songArtistRepoPort.findArtistIdsBySongId(song.getId()),
                songGenreRepoPort.findGenreIdsBySongId(song.getId()))))
            .toList();
        long total = songRepoPort.countByAlbumId(albumId);
        return buildPageResult(items, page, size, total);
        }

        @Override
        @Transactional(readOnly = true)
        public PageResultDto<SongResponse> getSongsByGenre(Long genreId, int page, int size) {
        if (!genreRepoPort.existsById(genreId)) {
            throw new GenreNotFoundException("Genre not found with id: " + genreId);
        }
        List<SongResponse> items = songRepoPort.findByGenreId(genreId, page, size).stream()
            .map(song -> songMapper.toResponse(withRelations(
                song,
                songArtistRepoPort.findArtistIdsBySongId(song.getId()),
                songGenreRepoPort.findGenreIdsBySongId(song.getId()))))
            .toList();
        long total = songRepoPort.countByGenreId(genreId);
        return buildPageResult(items, page, size, total);
        }

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<SongResponse> searchSongsByTitle(String query, String mode, int page, int size) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query is required");
        }
        String normalizedMode = mode == null ? "contains" : mode.trim().toLowerCase();
        List<Song> songs;
        long total;
        if ("fulltext".equals(normalizedMode)) {
            songs = songRepoPort.findByTitleFullText(query, page, size);
            total = songRepoPort.countByTitleFullText(query);
        } else {
            songs = songRepoPort.findByTitleContains(query, page, size);
            total = songRepoPort.countByTitleContains(query);
        }
        List<SongResponse> items = songs.stream()
                .map(song -> songMapper.toResponse(withRelations(
                        song,
                        songArtistRepoPort.findArtistIdsBySongId(song.getId()),
                        songGenreRepoPort.findGenreIdsBySongId(song.getId()))))
                .toList();
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

    private Song withRelations(Song song, List<UUID> artistIds, List<Long> genreIds) {
        return new Song(
                song.getId(),
                song.getTitle(),
                song.getSlug(),
                song.getDurationSeconds(),
                song.getAudioUrl(),
                song.getAudioFormat(),
                song.getAudioSizeBytes(),
                song.getCoverUrl(),
                song.getAlbumId(),
                song.getTrackNumber(),
                song.getReleaseDate(),
                song.getPlayCount(),
                song.isActive(),
                song.getCreatedBy(),
                song.getCreatedAt(),
                song.getUpdatedAt(),
                artistIds,
                genreIds
        );
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
        try {
            String objectName = storagePort.extractObjectName(bucket, url);
            storagePort.delete(bucket, objectName);
        } catch (RuntimeException ignored) {
            // Best-effort cleanup: deleting old objects should not fail update flow.
        }
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

    private Integer resolveDurationSeconds(Integer requestedDuration, MultipartFile audioFile, Integer fallbackDuration) {
        Integer extractedDuration = extractDurationSeconds(audioFile);
        if (extractedDuration != null && extractedDuration > 0) {
            return extractedDuration;
        }
        if (requestedDuration != null && requestedDuration > 0) {
            return requestedDuration;
        }
        if (fallbackDuration != null && fallbackDuration > 0) {
            return fallbackDuration;
        }
        throw new IllegalArgumentException("Cannot determine audio duration from uploaded file");
    }

    private Integer extractDurationSeconds(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            return null;
        }

        String extension = resolveAudioFormat(audioFile.getOriginalFilename());
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("ondas-audio-", "." + extension);
            try (var inputStream = audioFile.getInputStream()) {
                Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            AudioFile parsed = AudioFileIO.read(tempFile.toFile());
            int duration = parsed.getAudioHeader().getTrackLength();
            return duration > 0 ? duration : null;
        } catch (Exception ignored) {
            return null;
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                }
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
