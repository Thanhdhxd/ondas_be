package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.CreateSongRequest;
import com.example.ondas_be.application.dto.request.UpdateSongRequest;
import com.example.ondas_be.application.dto.response.SongResponse;
import com.example.ondas_be.application.mapper.SongMapper;
import com.example.ondas_be.application.service.impl.SongService;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.repoport.AlbumRepoPort;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.domain.repoport.GenreRepoPort;
import com.example.ondas_be.domain.repoport.SongArtistRepoPort;
import com.example.ondas_be.domain.repoport.SongGenreRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SongServiceTest {

    @Mock
    private SongRepoPort songRepoPort;

    @Mock
    private ArtistRepoPort artistRepoPort;

    @Mock
    private AlbumRepoPort albumRepoPort;

    @Mock
    private GenreRepoPort genreRepoPort;

    @Mock
    private SongArtistRepoPort songArtistRepoPort;

    @Mock
    private SongGenreRepoPort songGenreRepoPort;

    @Mock
    private StoragePort storagePort;

    @Mock
    private SongMapper songMapper;

    @InjectMocks
    private SongService songService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(songService, "audioBucket", "ondas-audio");
        ReflectionTestUtils.setField(songService, "imageBucket", "ondas-images");
    }

    @Test
    void createSong_WhenValid_ShouldUploadAndSave() {
        UUID albumId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        long genreId = 1L;

        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("My Song");
        request.setAlbumId(albumId);
        request.setTrackNumber(1);
        request.setReleaseDate(LocalDate.of(2026, 4, 18));
        request.setArtistIds(List.of(artistId));
        request.setGenreIds(List.of(genreId));

        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "song.mp3",
                "audio/mpeg",
                "audio-data".getBytes());
        MockMultipartFile coverFile = new MockMultipartFile(
                "cover",
                "cover.jpg",
                "image/jpeg",
                "image-data".getBytes());

        when(artistRepoPort.existsById(artistId)).thenReturn(true);
        when(genreRepoPort.existsById(genreId)).thenReturn(true);
        when(albumRepoPort.existsById(albumId)).thenReturn(true);
        when(songRepoPort.existsBySlug(any())).thenReturn(false);
        when(storagePort.upload(eq("ondas-audio"), any(), any(), anyLong(), any()))
                .thenReturn("audio-url");
        when(storagePort.upload(eq("ondas-images"), any(), any(), anyLong(), any()))
                .thenReturn("cover-url");
        when(songRepoPort.save(any(Song.class))).thenAnswer(invocation -> {
            Song input = invocation.getArgument(0);
            return new Song(
                    UUID.randomUUID(),
                    input.getTitle(),
                    input.getSlug(),
                    input.getDurationSeconds(),
                    input.getAudioUrl(),
                    input.getAudioFormat(),
                    input.getAudioSizeBytes(),
                    input.getCoverUrl(),
                    input.getAlbumId(),
                    input.getTrackNumber(),
                    input.getReleaseDate(),
                    input.getPlayCount(),
                    input.isActive(),
                    input.getCreatedBy(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    input.getArtistIds(),
                    input.getGenreIds()
            );
        });
        when(songMapper.toResponse(any(Song.class))).thenReturn(new SongResponse());

        songService.createSong(request, audioFile, coverFile);

        ArgumentCaptor<Song> songCaptor = ArgumentCaptor.forClass(Song.class);
        verify(songRepoPort).save(songCaptor.capture());
        Song savedSong = songCaptor.getValue();
        assertEquals("My Song", savedSong.getTitle());
        assertEquals("audio-url", savedSong.getAudioUrl());
        assertEquals("cover-url", savedSong.getCoverUrl());

        verify(songArtistRepoPort).replaceSongArtists(any(), eq(List.of(artistId)));
        verify(songGenreRepoPort).replaceSongGenres(any(), eq(List.of(genreId)));
    }

    @Test
    void createSong_WhenAudioMissing_ShouldThrowIllegalArgumentException() {
        CreateSongRequest request = new CreateSongRequest();
        request.setTitle("My Song");
        request.setArtistIds(List.of(UUID.randomUUID()));
        request.setGenreIds(List.of(1L));

        assertThrows(IllegalArgumentException.class, () -> songService.createSong(request, null, null));

        verify(storagePort, never()).upload(any(), any(), any(), anyLong(), any());
        verify(songRepoPort, never()).save(any());
    }

    @Test
    void updateSong_WhenReplaceAudioCover_ShouldDeleteOldObjects() {
        UUID songId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        long genreId = 1L;

        Song existing = new Song(
                songId,
                "Old Song",
                "old-song",
                180,
                "old-audio-url",
                "mp3",
                123L,
                "old-cover-url",
                null,
                1,
                null,
                0L,
                true,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(artistId),
                List.of(genreId)
        );

        UpdateSongRequest request = new UpdateSongRequest();
        request.setTitle("New Song");
        request.setArtistIds(List.of(artistId));
        request.setGenreIds(List.of(genreId));

        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "new.mp3",
                "audio/mpeg",
                "new-audio".getBytes());
        MockMultipartFile coverFile = new MockMultipartFile(
                "cover",
                "new.jpg",
                "image/jpeg",
                "new-cover".getBytes());

        when(songRepoPort.findById(songId)).thenReturn(java.util.Optional.of(existing));
        when(artistRepoPort.existsById(artistId)).thenReturn(true);
        when(genreRepoPort.existsById(genreId)).thenReturn(true);
        when(songRepoPort.existsBySlug(any())).thenReturn(false);
        when(storagePort.upload(eq("ondas-audio"), any(), any(), anyLong(), any()))
                .thenReturn("new-audio-url");
        when(storagePort.upload(eq("ondas-images"), any(), any(), anyLong(), any()))
                .thenReturn("new-cover-url");
        when(storagePort.extractObjectName(eq("ondas-audio"), eq("old-audio-url"))).thenReturn("old.mp3");
        when(storagePort.extractObjectName(eq("ondas-images"), eq("old-cover-url"))).thenReturn("old.jpg");
        when(songRepoPort.save(any(Song.class))).thenReturn(existing);
        when(songMapper.toResponse(any(Song.class))).thenReturn(new SongResponse());

        songService.updateSong(songId, request, audioFile, coverFile);

        verify(storagePort).delete("ondas-audio", "old.mp3");
        verify(storagePort).delete("ondas-images", "old.jpg");
    }
}
