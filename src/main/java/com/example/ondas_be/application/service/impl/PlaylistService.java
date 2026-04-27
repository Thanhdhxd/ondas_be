package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AddSongToPlaylistRequest;
import com.example.ondas_be.application.dto.request.CreatePlaylistRequest;
import com.example.ondas_be.application.dto.request.PlaylistFilterRequest;
import com.example.ondas_be.application.dto.request.ReorderPlaylistSongsRequest;
import com.example.ondas_be.application.dto.request.UpdatePlaylistRequest;
import com.example.ondas_be.application.dto.response.PlaylistResponse;
import com.example.ondas_be.application.dto.response.PlaylistSongInfoResponse;
import com.example.ondas_be.application.dto.response.PlaylistSongResponse;
import com.example.ondas_be.application.exception.InvalidCredentialsException;
import com.example.ondas_be.application.exception.PlaylistAccessDeniedException;
import com.example.ondas_be.application.exception.PlaylistNotFoundException;
import com.example.ondas_be.application.exception.PlaylistReorderInvalidException;
import com.example.ondas_be.application.exception.PlaylistSongAlreadyExistsException;
import com.example.ondas_be.application.exception.PlaylistSongNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.exception.StorageOperationException;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.mapper.PlaylistMapper;
import com.example.ondas_be.application.service.port.PlaylistServicePort;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.domain.entity.Playlist;
import com.example.ondas_be.domain.entity.PlaylistSong;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.PlaylistRepoPort;
import com.example.ondas_be.domain.repoport.PlaylistSongRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
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
public class PlaylistService implements PlaylistServicePort {

    private final PlaylistRepoPort playlistRepoPort;
    private final PlaylistSongRepoPort playlistSongRepoPort;
    private final SongRepoPort songRepoPort;
    private final UserRepoPort userRepoPort;
    private final StoragePort storagePort;
    private final PlaylistMapper playlistMapper;

    @Value("${storage.minio.bucket-image}")
    private String imageBucket;

    @Override
    @Transactional
    public PlaylistResponse createPlaylist(String email, CreatePlaylistRequest request, MultipartFile coverFile) {
        User user = resolveUser(email);

        Playlist playlist = new Playlist(
                null,
                user.getId(),
                request.getName().trim(),
                request.getDescription(),
                uploadOptionalImage(coverFile, "playlists/cover/"),
                Boolean.TRUE.equals(request.getIsPublic()),
                0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Playlist saved = playlistRepoPort.save(playlist);
        return buildPlaylistResponse(saved, true);
    }

    @Override
    @Transactional
    public PlaylistResponse updatePlaylist(String email, UUID id, UpdatePlaylistRequest request, MultipartFile coverFile) {
        Playlist existing = ensureOwner(email, id);

        String coverUrl = existing.getCoverUrl();
        if (coverFile != null && !coverFile.isEmpty()) {
            coverUrl = uploadOptionalImage(coverFile, "playlists/cover/");
            deleteObject(existing.getCoverUrl());
        }

        Playlist updated = new Playlist(
                existing.getId(),
                existing.getUserId(),
                request.getName() != null ? request.getName().trim() : existing.getName(),
                request.getDescription() != null ? request.getDescription() : existing.getDescription(),
                coverUrl,
                request.getIsPublic() != null ? request.getIsPublic() : existing.isPublic(),
                existing.getTotalSongs(),
                existing.getCreatedAt(),
                existing.getUpdatedAt()
        );

        Playlist saved = playlistRepoPort.save(updated);
        return buildPlaylistResponse(saved, true);
    }

    @Override
    @Transactional(readOnly = true)
    public PlaylistResponse getPlaylistById(String email, UUID id) {
        Playlist playlist = getPlaylistOrThrow(id);

        UUID requesterId = resolveUserIdIfAuthenticated(email);
        if (!playlist.isPublic() && (requesterId == null || !playlist.getUserId().equals(requesterId))) {
            throw new PlaylistAccessDeniedException("You do not have permission to access this playlist");
        }

        return buildPlaylistResponse(playlist, true);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<PlaylistResponse> getPlaylists(String email, PlaylistFilterRequest filter) {
        int page = Math.max(0, filter.getPage());
        int size = Math.max(1, filter.getSize());
        String query = filter.getQuery();

        List<Playlist> playlists;
        long total;

        if (Boolean.TRUE.equals(filter.getOwner())) {
            User user = resolveUser(email);
            playlists = playlistRepoPort.findByUserId(user.getId(), filter.getIsPublic(), query, page, size);
            total = playlistRepoPort.countByUserId(user.getId(), filter.getIsPublic(), query);
        } else {
            if (Boolean.FALSE.equals(filter.getIsPublic())) {
                throw new IllegalArgumentException("isPublic=false requires owner=true");
            }
            playlists = playlistRepoPort.findPublic(query, page, size);
            total = playlistRepoPort.countPublic(query);
        }

        List<PlaylistResponse> items = playlists.stream()
                .map(playlist -> buildPlaylistResponse(playlist, false))
                .toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return PageResultDto.<PlaylistResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    @Override
    @Transactional
    public void deletePlaylist(String email, UUID id) {
        Playlist playlist = ensureOwner(email, id);
        deleteObject(playlist.getCoverUrl());
        playlistRepoPort.deleteById(id);
    }

    @Override
    @Transactional
    public PlaylistResponse addSongToPlaylist(String email, UUID id, AddSongToPlaylistRequest request) {
        Playlist playlist = ensureOwner(email, id);

        UUID songId = request.getSongId();
        if (songRepoPort.findById(songId).isEmpty()) {
            throw new SongNotFoundException("Song not found with id: " + songId);
        }
        if (playlistSongRepoPort.existsByPlaylistIdAndSongId(id, songId)) {
            throw new PlaylistSongAlreadyExistsException("Song already exists in playlist");
        }

        int nextPosition = playlistSongRepoPort.findMaxPositionByPlaylistId(id) + 1;
        playlistSongRepoPort.save(new PlaylistSong(id, songId, nextPosition, null));
        syncTotalSongs(playlist);

        return buildPlaylistResponse(getPlaylistOrThrow(id), true);
    }

    @Override
    @Transactional
    public PlaylistResponse removeSongFromPlaylist(String email, UUID id, UUID songId) {
        Playlist playlist = ensureOwner(email, id);

        if (!playlistSongRepoPort.existsByPlaylistIdAndSongId(id, songId)) {
            throw new PlaylistSongNotFoundException("Song is not in playlist: " + songId);
        }

        playlistSongRepoPort.deleteByPlaylistIdAndSongId(id, songId);
        syncTotalSongs(playlist);
        compactSongOrder(id);

        return buildPlaylistResponse(getPlaylistOrThrow(id), true);
    }

    @Override
    @Transactional
    public PlaylistResponse reorderPlaylistSongs(String email, UUID id, ReorderPlaylistSongsRequest request) {
        ensureOwner(email, id);

        List<UUID> newOrder = request.getSongIds();
        validateNoDuplicates(newOrder);

        List<UUID> existing = playlistSongRepoPort.findSongIdsByPlaylistId(id);
        if (existing.size() != newOrder.size() || !new HashSet<>(existing).equals(new HashSet<>(newOrder))) {
            throw new PlaylistReorderInvalidException("Reorder payload must contain all existing playlist songs exactly once");
        }

        playlistSongRepoPort.updateSongOrder(id, newOrder);
        return buildPlaylistResponse(getPlaylistOrThrow(id), true);
    }

    private Playlist ensureOwner(String email, UUID playlistId) {
        User user = resolveUser(email);
        Playlist playlist = getPlaylistOrThrow(playlistId);

        if (!playlist.getUserId().equals(user.getId())) {
            throw new PlaylistAccessDeniedException("You do not have permission to modify this playlist");
        }
        return playlist;
    }

    private Playlist getPlaylistOrThrow(UUID id) {
        return playlistRepoPort.findById(id)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist not found with id: " + id));
    }

    private User resolveUser(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
        return userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    private UUID resolveUserIdIfAuthenticated(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepoPort.findByEmail(email).map(User::getId).orElse(null);
    }

    private PlaylistResponse buildPlaylistResponse(Playlist playlist, boolean includeSongs) {
        PlaylistResponse response = playlistMapper.toResponse(playlist);
        if (includeSongs) {
            response.setSongs(buildSongItems(playlist.getId()));
        } else {
            response.setSongs(List.of());
        }
        return response;
    }

    private List<PlaylistSongResponse> buildSongItems(UUID playlistId) {
        List<PlaylistSong> playlistSongs = playlistSongRepoPort.findByPlaylistIdOrderByPosition(playlistId);
        if (playlistSongs.isEmpty()) {
            return List.of();
        }
        List<UUID> songIds = playlistSongs.stream().map(PlaylistSong::getSongId).distinct().toList();
        Map<UUID, Song> songMap = songRepoPort.findByIds(songIds).stream()
                .collect(Collectors.toMap(Song::getId, Function.identity()));

        return playlistSongs.stream().map(item -> PlaylistSongResponse.builder()
                .position(item.getPosition())
                .addedAt(item.getAddedAt())
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

    private void syncTotalSongs(Playlist playlist) {
        int totalSongs = (int) playlistSongRepoPort.countByPlaylistId(playlist.getId());
        Playlist synced = new Playlist(
                playlist.getId(),
                playlist.getUserId(),
                playlist.getName(),
                playlist.getDescription(),
                playlist.getCoverUrl(),
                playlist.isPublic(),
                totalSongs,
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
        playlistRepoPort.save(synced);
    }

    private void compactSongOrder(UUID playlistId) {
        List<UUID> orderedSongIds = playlistSongRepoPort.findSongIdsByPlaylistId(playlistId);
        playlistSongRepoPort.updateSongOrder(playlistId, orderedSongIds);
    }

    private void validateNoDuplicates(List<UUID> songIds) {
        if (songIds.size() != new HashSet<>(songIds).size()) {
            throw new PlaylistReorderInvalidException("Song IDs in reorder payload must be unique");
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
