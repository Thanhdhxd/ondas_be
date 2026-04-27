package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.response.PlayHistoryResponse;
import com.example.ondas_be.application.dto.response.PlayHistorySongInfo;
import com.example.ondas_be.application.exception.PlayHistoryNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.service.port.PlayHistoryServicePort;
import com.example.ondas_be.domain.entity.PlayHistory;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.PlayHistoryRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayHistoryService implements PlayHistoryServicePort {

    private final PlayHistoryRepoPort playHistoryRepoPort;
    private final SongRepoPort songRepoPort;
    private final UserRepoPort userRepoPort;

    @Override
    public PageResultDto<PlayHistoryResponse> getMyHistory(String email, int page, int size) {
        User user = resolveUser(email);

        List<PlayHistory> entries = playHistoryRepoPort.findByUserId(user.getId(), page, size);
        long total = playHistoryRepoPort.countByUserId(user.getId());

        // Batch-fetch song info để tránh N+1 queries
        List<UUID> songIds = entries.stream().map(PlayHistory::getSongId).distinct().toList();
        Map<UUID, Song> songMap = songRepoPort.findByIds(songIds).stream()
                .collect(Collectors.toMap(Song::getId, Function.identity()));

        List<PlayHistoryResponse> items = entries.stream()
                .map(entry -> {
                    Song song = songMap.get(entry.getSongId());
                    PlayHistorySongInfo songInfo = song != null ? toSongInfo(song) : null;
                    return toResponse(entry, songInfo);
                })
                .toList();

        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;

        return PageResultDto.<PlayHistoryResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    @Override
    @Transactional
    public void clearMyHistory(String email) {
        User user = resolveUser(email);
        playHistoryRepoPort.deleteAllByUserId(user.getId());
    }

    @Override
    @Transactional
    public void deleteHistoryEntry(String email, Long id) {
        User user = resolveUser(email);

        playHistoryRepoPort.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new PlayHistoryNotFoundException(
                        "Play history entry not found with id: " + id));

        playHistoryRepoPort.deleteByIdAndUserId(id, user.getId());
    }

    @Override
    @Transactional
    public void recordPlay(UUID songId, String email, String source) {
        User user = resolveUser(email);
        Song song = songRepoPort.findById(songId)
                .orElseThrow(() -> new SongNotFoundException("Song not found with id: " + songId));
        if (!song.isActive()) {
            throw new SongNotFoundException("Song not found with id: " + songId);
        }
        PlayHistory history = new PlayHistory(null, user.getId(), songId, null, source);
        playHistoryRepoPort.save(history);
        songRepoPort.incrementPlayCount(songId);
    }

    // ---- helpers ----

    private User resolveUser(String email) {
        return userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }

    private PlayHistorySongInfo toSongInfo(Song song) {
        return PlayHistorySongInfo.builder()
                .id(song.getId())
                .title(song.getTitle())
                .coverUrl(song.getCoverUrl())
                .durationSeconds(song.getDurationSeconds())
                .audioUrl(song.getAudioUrl())
                .build();
    }

    private PlayHistoryResponse toResponse(PlayHistory entry, PlayHistorySongInfo songInfo) {
        return PlayHistoryResponse.builder()
                .id(entry.getId())
                .song(songInfo)
                .playedAt(entry.getPlayedAt())
                .source(entry.getSource())
                .build();
    }
}
