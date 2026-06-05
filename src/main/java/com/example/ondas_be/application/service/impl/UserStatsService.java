package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.response.ArtistSummaryResponse;
import com.example.ondas_be.application.dto.response.UserListeningTimeResponse;
import com.example.ondas_be.application.dto.response.UserTopArtistResponse;
import com.example.ondas_be.application.dto.response.UserTopSongResponse;
import com.example.ondas_be.application.exception.UserNotFoundException;
import com.example.ondas_be.application.mapper.ArtistMapper;
import com.example.ondas_be.application.service.port.UserStatsServicePort;
import com.example.ondas_be.domain.entity.Artist;
import com.example.ondas_be.domain.entity.ArtistPlayCount;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.entity.SongPlayCount;
import com.example.ondas_be.domain.entity.User;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.domain.repoport.PlayHistoryRepoPort;
import com.example.ondas_be.domain.repoport.SongArtistRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import com.example.ondas_be.domain.repoport.UserRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserStatsService implements UserStatsServicePort {

    private final PlayHistoryRepoPort playHistoryRepoPort;
    private final UserRepoPort userRepoPort;
    private final SongRepoPort songRepoPort;
    private final SongArtistRepoPort songArtistRepoPort;
    private final ArtistRepoPort artistRepoPort;
    private final ArtistMapper artistMapper;

    @Override
    @Transactional(readOnly = true)
    public UserListeningTimeResponse getMyListeningTime(String email) {
        User user = resolveUser(email);
        long totalSeconds = playHistoryRepoPort.sumListeningDurationByUserId(user.getId());
        long totalSongs = playHistoryRepoPort.countByUserId(user.getId());

        double totalMinutes = (double) totalSeconds / 60.0;
        double totalHours = (double) totalSeconds / 3600.0;

        // Round to 2 decimal places
        totalMinutes = Math.round(totalMinutes * 100.0) / 100.0;
        totalHours = Math.round(totalHours * 100.0) / 100.0;

        return UserListeningTimeResponse.builder()
                .totalListeningSeconds(totalSeconds)
                .totalListeningMinutes(totalMinutes)
                .totalListeningHours(totalHours)
                .totalSongsPlayed(totalSongs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserTopSongResponse> getMyTopSongs(String email, int limit) {
        User user = resolveUser(email);
        List<SongPlayCount> topSongPlayCounts = playHistoryRepoPort.findTopSongsByUserId(user.getId(), limit);
        if (topSongPlayCounts.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> songIds = topSongPlayCounts.stream().map(SongPlayCount::getSongId).toList();
        Map<UUID, Song> songById = songRepoPort.findByIds(songIds).stream()
                .collect(Collectors.toMap(Song::getId, song -> song));

        Map<UUID, List<UUID>> artistIdsBySong = songArtistRepoPort.findArtistIdsBySongIds(songIds);
        List<UUID> allArtistIds = artistIdsBySong.values().stream()
                .flatMap(List::stream).distinct().toList();
        Map<UUID, ArtistSummaryResponse> artistById = artistRepoPort.findByIds(allArtistIds).stream()
                .collect(Collectors.toMap(Artist::getId, artistMapper::toSummaryResponse));

        return topSongPlayCounts.stream()
                .map(item -> {
                    Song song = songById.get(item.getSongId());
                    if (song == null) {
                        return null;
                    }
                    List<ArtistSummaryResponse> artists = artistIdsBySong
                            .getOrDefault(song.getId(), Collections.emptyList()).stream()
                            .map(artistById::get)
                            .filter(Objects::nonNull)
                            .toList();
                    return UserTopSongResponse.builder()
                            .id(song.getId())
                            .title(song.getTitle())
                            .coverUrl(song.getCoverUrl())
                            .playCount(item.getPlayCount())
                            .artists(artists)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserTopArtistResponse> getMyTopArtists(String email, int limit) {
        User user = resolveUser(email);
        List<ArtistPlayCount> topArtistPlayCounts = playHistoryRepoPort.findTopArtistsByUserId(user.getId(), limit);
        if (topArtistPlayCounts.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> artistIds = topArtistPlayCounts.stream().map(ArtistPlayCount::getArtistId).toList();
        Map<UUID, Artist> artistById = artistRepoPort.findByIds(artistIds).stream()
                .collect(Collectors.toMap(Artist::getId, artist -> artist));

        return topArtistPlayCounts.stream()
                .map(item -> {
                    Artist artist = artistById.get(item.getArtistId());
                    if (artist == null) {
                        return null;
                    }
                    return UserTopArtistResponse.builder()
                            .id(artist.getId())
                            .name(artist.getName())
                            .avatarUrl(artist.getAvatarUrl())
                            .playCount(item.getPlayCount())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private User resolveUser(String email) {
        return userRepoPort.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
    }
}
