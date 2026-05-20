package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AddSongToSystemPlaylistRequest;
import com.example.ondas_be.application.dto.request.CreateSystemPlaylistRequest;
import com.example.ondas_be.application.dto.request.ReorderSystemPlaylistSongsRequest;
import com.example.ondas_be.application.dto.request.SystemPlaylistFilterRequest;
import com.example.ondas_be.application.dto.request.UpdateSystemPlaylistRequest;
import com.example.ondas_be.application.dto.response.PlaylistSongInfoResponse;
import com.example.ondas_be.application.dto.response.SystemPlaylistResponse;
import com.example.ondas_be.application.dto.response.SystemPlaylistSongResponse;
import com.example.ondas_be.application.exception.InvalidCredentialsException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.exception.StorageOperationException;
import com.example.ondas_be.application.exception.SystemPlaylistNotFoundException;
import com.example.ondas_be.application.exception.SystemPlaylistReorderInvalidException;
import com.example.ondas_be.application.exception.SystemPlaylistSongAlreadyExistsException;
import com.example.ondas_be.application.exception.SystemPlaylistSongNotFoundException;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.mapper.SystemPlaylistMapper;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.application.service.port.SystemPlaylistServicePort;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.entity.SystemPlaylist;
import com.example.ondas_be.domain.entity.SystemPlaylistSong;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import com.example.ondas_be.domain.repoport.SystemPlaylistRepoPort;
import com.example.ondas_be.domain.repoport.SystemPlaylistSongRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemPlaylistService implements SystemPlaylistServicePort {

    private final SystemPlaylistRepoPort systemPlaylistRepoPort;
    private final SystemPlaylistSongRepoPort systemPlaylistSongRepoPort;
    private final SongRepoPort songRepoPort;
    private final UserRepoPort userRepoPort;
    private final StoragePort storagePort;
    private final SystemPlaylistMapper systemPlaylistMapper;

    @Value("${storage.minio.bucket-image}")
    private String imageBucket;

    @Override
    @Transactional
    public SystemPlaylistResponse createSystemPlaylist(
            String email,
            CreateSystemPlaylistRequest request,
            MultipartFile coverFile
    ) {
        User user = resolveUser(email);

        SystemPlaylist playlist = new SystemPlaylist(
                null,
                request.getName().trim(),
                request.getDescription(),
                uploadOptionalImage(coverFile, "system-playlists/cover/"),
                Boolean.TRUE.equals(request.getIsActive()),
                user.getId(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        SystemPlaylist saved = systemPlaylistRepoPort.save(playlist);
        return buildSystemPlaylistResponse(saved, true);
    }

    @Override
    @Transactional
    public SystemPlaylistResponse updateSystemPlaylist(
            String email,
            UUID id,
            UpdateSystemPlaylistRequest request,
            MultipartFile coverFile
    ) {
        resolveUser(email);
        SystemPlaylist existing = getSystemPlaylistOrThrow(id);

        String coverUrl = existing.getCoverUrl();
        if (coverFile != null && !coverFile.isEmpty()) {
            coverUrl = uploadOptionalImage(coverFile, "system-playlists/cover/");
            deleteObject(existing.getCoverUrl());
        }

        SystemPlaylist updated = new SystemPlaylist(
                existing.getId(),
                request.getName() != null ? request.getName().trim() : existing.getName(),
                request.getDescription() != null ? request.getDescription() : existing.getDescription(),
                coverUrl,
                request.getIsActive() != null ? request.getIsActive() : existing.isActive(),
                existing.getCreatedBy(),
                existing.getCreatedAt(),
                existing.getUpdatedAt()
        );

        SystemPlaylist saved = systemPlaylistRepoPort.save(updated);
        return buildSystemPlaylistResponse(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemPlaylistResponse getSystemPlaylistById(UUID id) {
        SystemPlaylist playlist = getSystemPlaylistOrThrow(id);
        return buildSystemPlaylistResponse(playlist, true);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemPlaylistResponse getActiveSystemPlaylistById(UUID id) {
        SystemPlaylist playlist = getSystemPlaylistOrThrow(id);
        if (!playlist.isActive()) {
            throw new SystemPlaylistNotFoundException("System playlist not found with id: " + id);
        }
        return buildSystemPlaylistResponse(playlist, true);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<SystemPlaylistResponse> getSystemPlaylists(SystemPlaylistFilterRequest filter) {
        int page = Math.max(0, filter.getPage());
        int size = Math.max(1, filter.getSize());
        String query = filter.getQuery();

        List<SystemPlaylist> playlists = systemPlaylistRepoPort.findAll(
                query,
                filter.getIsActive(),
                page,
                size
        );
        long total = systemPlaylistRepoPort.countAll(query, filter.getIsActive());

        List<SystemPlaylistResponse> items = playlists.stream()
                .map(playlist -> buildSystemPlaylistResponse(playlist, false))
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return PageResultDto.<SystemPlaylistResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<SystemPlaylistResponse> getActiveSystemPlaylists(SystemPlaylistFilterRequest filter) {
        int page = Math.max(0, filter.getPage());
        int size = Math.max(1, filter.getSize());
        String query = filter.getQuery();

        List<SystemPlaylist> playlists = systemPlaylistRepoPort.findActive(query, page, size);
        long total = systemPlaylistRepoPort.countActive(query);

        List<SystemPlaylistResponse> items = playlists.stream()
                .map(playlist -> buildSystemPlaylistResponse(playlist, false))
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return PageResultDto.<SystemPlaylistResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    @Override
    @Transactional
    public void deleteSystemPlaylist(UUID id) {
        SystemPlaylist playlist = getSystemPlaylistOrThrow(id);
        deleteObject(playlist.getCoverUrl());
        systemPlaylistRepoPort.deleteById(id);
    }

    @Override
    @Transactional
    public SystemPlaylistResponse addSongToSystemPlaylist(UUID id, AddSongToSystemPlaylistRequest request) {
        getSystemPlaylistOrThrow(id);

        UUID songId = request.getSongId();
        if (!songRepoPort.existsById(songId)) {
            throw new SongNotFoundException("Song not found with id: " + songId);
        }
        if (systemPlaylistSongRepoPort.existsByPlaylistIdAndSongId(id, songId)) {
            throw new SystemPlaylistSongAlreadyExistsException("Song already exists in system playlist");
        }

        int maxPos = systemPlaylistSongRepoPort.findMaxPositionByPlaylistId(id);
        int nextPosition = maxPos + 1;
        systemPlaylistSongRepoPort.save(new SystemPlaylistSong(id, songId, nextPosition));

        return buildSystemPlaylistResponse(getSystemPlaylistOrThrow(id), true);
    }

    @Override
    @Transactional
    public SystemPlaylistResponse removeSongFromSystemPlaylist(UUID id, UUID songId) {
        getSystemPlaylistOrThrow(id);

        if (!systemPlaylistSongRepoPort.existsByPlaylistIdAndSongId(id, songId)) {
            throw new SystemPlaylistSongNotFoundException("Song is not in system playlist: " + songId);
        }

        systemPlaylistSongRepoPort.deleteByPlaylistIdAndSongId(id, songId);
        compactSongOrder(id);

        return buildSystemPlaylistResponse(getSystemPlaylistOrThrow(id), true);
    }

    @Override
    @Transactional
    public SystemPlaylistResponse reorderSystemPlaylistSongs(UUID id, ReorderSystemPlaylistSongsRequest request) {
        getSystemPlaylistOrThrow(id);

        List<UUID> newOrder = request.getSongIds();
        validateNoDuplicates(newOrder);

        List<UUID> existing = systemPlaylistSongRepoPort.findSongIdsByPlaylistId(id);
        if (existing.size() != newOrder.size() || !new HashSet<>(existing).equals(new HashSet<>(newOrder))) {
            throw new SystemPlaylistReorderInvalidException("Reorder payload must contain all existing playlist songs exactly once");
        }

        systemPlaylistSongRepoPort.updateSongOrder(id, newOrder);
        return buildSystemPlaylistResponse(getSystemPlaylistOrThrow(id), true);
    }

    private SystemPlaylist getSystemPlaylistOrThrow(UUID id) {
        return systemPlaylistRepoPort.findById(id)
                .orElseThrow(() -> new SystemPlaylistNotFoundException("System playlist not found with id: " + id));
    }

    private User resolveUser(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        return userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    private SystemPlaylistResponse buildSystemPlaylistResponse(SystemPlaylist playlist, boolean includeSongs) {
        SystemPlaylistResponse response = systemPlaylistMapper.toResponse(playlist);
        int totalSongs = (int) systemPlaylistSongRepoPort.countByPlaylistId(playlist.getId());
        response.setTotalSongs(totalSongs);
        if (includeSongs) {
            response.setSongs(buildSongItems(playlist.getId()));
        } else {
            response.setSongs(List.of());
        }
        return response;
    }

    private List<SystemPlaylistSongResponse> buildSongItems(UUID playlistId) {
        List<SystemPlaylistSong> playlistSongs = systemPlaylistSongRepoPort.findByPlaylistIdOrderByPosition(playlistId);
        if (playlistSongs.isEmpty()) {
            return List.of();
        }
        List<UUID> songIds = playlistSongs.stream().map(SystemPlaylistSong::getSongId).distinct().toList();
        Map<UUID, Song> songMap = songRepoPort.findByIds(songIds).stream()
                .collect(Collectors.toMap(Song::getId, Function.identity()));

        return playlistSongs.stream().map(item -> SystemPlaylistSongResponse.builder()
                .position(item.getPosition())
                .song(toSongInfo(songMap.get(item.getSongId())))
                .build()).toList();
    }

    private PlaylistSongInfoResponse toSongInfo(Song song) {
        if (song == null) {
            return null;
        }
        return PlaylistSongInfoResponse.builder()
                .id(song.getId())
                .title(song.getTitle())
                .coverUrl(song.getCoverUrl())
                .durationSeconds(song.getDurationSeconds())
                .audioUrl(song.getAudioUrl())
                .build();
    }

    private void compactSongOrder(UUID playlistId) {
        List<UUID> orderedSongIds = systemPlaylistSongRepoPort.findSongIdsByPlaylistId(playlistId);
        systemPlaylistSongRepoPort.updateSongOrder(playlistId, orderedSongIds);
    }

    private void validateNoDuplicates(List<UUID> songIds) {
        if (songIds.size() != new HashSet<>(songIds).size()) {
            throw new SystemPlaylistReorderInvalidException("Song IDs in reorder payload must be unique");
        }
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
        if (url == null || url.isBlank()) {
            return;
        }
        String objectName = storagePort.extractObjectName(imageBucket, url);
        storagePort.delete(imageBucket, objectName);
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
    }
}
