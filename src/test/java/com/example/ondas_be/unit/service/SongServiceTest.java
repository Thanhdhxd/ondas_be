package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.CreateSongRequest;
import com.example.ondas_be.application.dto.request.SongFilterRequest;
import com.example.ondas_be.application.dto.request.UpdateSongRequest;
import com.example.ondas_be.application.dto.response.SongResponse;
import com.example.ondas_be.application.dto.response.SongStreamResponse;
import com.example.ondas_be.application.exception.AlbumNotFoundException;
import com.example.ondas_be.application.exception.ArtistNotFoundException;
import com.example.ondas_be.application.exception.GenreNotFoundException;
import com.example.ondas_be.application.exception.SongNotFoundException;
import com.example.ondas_be.application.mapper.ArtistMapper;
import com.example.ondas_be.application.mapper.GenreMapper;
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

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @Mock
    private ArtistMapper artistMapper;

    @Mock
    private GenreMapper genreMapper;

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
        when(artistMapper.toSummaryResponseList(any())).thenReturn(List.of());
        when(genreMapper.toSummaryResponseList(any())).thenReturn(List.of());

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
        void createSong_WhenCoverMissing_ShouldSkipCoverUpload() {
                UUID albumId = UUID.randomUUID();
                UUID artistId = UUID.randomUUID();
                long genreId = 1L;

                CreateSongRequest request = new CreateSongRequest();
                request.setTitle("My Song");
                request.setAlbumId(albumId);
                request.setArtistIds(List.of(artistId));
                request.setGenreIds(List.of(genreId));

                MockMultipartFile audioFile = new MockMultipartFile(
                                "audio",
                                "song.mp3",
                                "audio/mpeg",
                                "audio-data".getBytes());

                when(artistRepoPort.existsById(artistId)).thenReturn(true);
                when(genreRepoPort.existsById(genreId)).thenReturn(true);
                when(albumRepoPort.existsById(albumId)).thenReturn(true);
                when(songRepoPort.existsBySlug(any())).thenReturn(false);
                when(storagePort.upload(eq("ondas-audio"), any(), any(), anyLong(), any()))
                                .thenReturn("audio-url");
                when(songRepoPort.save(any(Song.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(songMapper.toResponse(any(Song.class))).thenReturn(new SongResponse());
                when(artistMapper.toSummaryResponseList(any())).thenReturn(List.of());
                when(genreMapper.toSummaryResponseList(any())).thenReturn(List.of());

                songService.createSong(request, audioFile, null);

                verify(storagePort, never()).upload(eq("ondas-images"), any(), any(), anyLong(), any());
        }

        @Test
        void createSong_WhenArtistMissing_ShouldThrowArtistNotFoundException() {
                CreateSongRequest request = new CreateSongRequest();
                request.setTitle("My Song");
                request.setArtistIds(List.of(UUID.randomUUID()));
                request.setGenreIds(List.of(1L));

                MockMultipartFile audioFile = new MockMultipartFile(
                                "audio",
                                "song.mp3",
                                "audio/mpeg",
                                "audio-data".getBytes());

                when(artistRepoPort.existsById(any())).thenReturn(false);

                assertThrows(ArtistNotFoundException.class, () -> songService.createSong(request, audioFile, null));
        }

        @Test
        void createSong_WhenGenreMissing_ShouldThrowGenreNotFoundException() {
                CreateSongRequest request = new CreateSongRequest();
                request.setTitle("My Song");
                request.setArtistIds(List.of(UUID.randomUUID()));
                request.setGenreIds(List.of(1L));

                MockMultipartFile audioFile = new MockMultipartFile(
                                "audio",
                                "song.mp3",
                                "audio/mpeg",
                                "audio-data".getBytes());

                when(artistRepoPort.existsById(any())).thenReturn(true);
                when(genreRepoPort.existsById(any())).thenReturn(false);

                assertThrows(GenreNotFoundException.class, () -> songService.createSong(request, audioFile, null));
        }

        @Test
        void createSong_WhenAlbumMissing_ShouldThrowAlbumNotFoundException() {
                UUID albumId = UUID.randomUUID();

                CreateSongRequest request = new CreateSongRequest();
                request.setTitle("My Song");
                request.setAlbumId(albumId);
                request.setArtistIds(List.of(UUID.randomUUID()));
                request.setGenreIds(List.of(1L));

                MockMultipartFile audioFile = new MockMultipartFile(
                                "audio",
                                "song.mp3",
                                "audio/mpeg",
                                "audio-data".getBytes());

                when(artistRepoPort.existsById(any())).thenReturn(true);
                when(genreRepoPort.existsById(any())).thenReturn(true);
                when(albumRepoPort.existsById(albumId)).thenReturn(false);

                assertThrows(AlbumNotFoundException.class, () -> songService.createSong(request, audioFile, null));
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
        void updateSong_WhenNotFound_ShouldThrowSongNotFoundException() {
                UpdateSongRequest request = new UpdateSongRequest();
                UUID songId = UUID.randomUUID();

                when(songRepoPort.findById(songId)).thenReturn(java.util.Optional.empty());

                assertThrows(SongNotFoundException.class, () -> songService.updateSong(songId, request, null, null));
        }

        @Test
        void updateSong_WhenMetadataOnly_ShouldNotDeleteMedia() {
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

                when(songRepoPort.findById(songId)).thenReturn(java.util.Optional.of(existing));
                when(songRepoPort.existsBySlug(any())).thenReturn(false);
                when(songRepoPort.save(any(Song.class))).thenReturn(existing);
                when(songMapper.toResponse(any(Song.class))).thenReturn(new SongResponse());
                when(songArtistRepoPort.findArtistIdsBySongId(songId)).thenReturn(List.of(artistId));
                when(songGenreRepoPort.findGenreIdsBySongId(songId)).thenReturn(List.of(genreId));
                when(artistMapper.toSummaryResponseList(any())).thenReturn(List.of());
                when(genreMapper.toSummaryResponseList(any())).thenReturn(List.of());

                songService.updateSong(songId, request, null, null);

                verify(storagePort, never()).delete(eq("ondas-audio"), any());
                verify(storagePort, never()).delete(eq("ondas-images"), any());
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
        when(artistMapper.toSummaryResponseList(any())).thenReturn(List.of());
        when(genreMapper.toSummaryResponseList(any())).thenReturn(List.of());

        songService.updateSong(songId, request, audioFile, coverFile);

        verify(storagePort).delete("ondas-audio", "old.mp3");
        verify(storagePort).delete("ondas-images", "old.jpg");
    }

        @Test
        void getSongById_WhenValid_ShouldReturnResponse() {
                UUID songId = UUID.randomUUID();
                UUID artistId = UUID.randomUUID();
                long genreId = 1L;

                Song song = new Song(
                                songId,
                                "Song",
                                "song",
                                180,
                                "audio-url",
                                "mp3",
                                123L,
                                "cover-url",
                                null,
                                1,
                                LocalDate.now(),
                                0L,
                                true,
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                List.of(artistId),
                                List.of(genreId)
                );

                when(songRepoPort.findById(songId)).thenReturn(java.util.Optional.of(song));
                when(songArtistRepoPort.findArtistIdsBySongId(songId)).thenReturn(List.of(artistId));
                when(songGenreRepoPort.findGenreIdsBySongId(songId)).thenReturn(List.of(genreId));
                when(songMapper.toResponse(song)).thenReturn(new SongResponse());
                when(artistMapper.toSummaryResponseList(any())).thenReturn(List.of());
                when(genreMapper.toSummaryResponseList(any())).thenReturn(List.of());

                SongResponse response = songService.getSongById(songId);

                assertEquals(SongResponse.class, response.getClass());
        }

        @Test
        void getSongById_WhenNotFound_ShouldThrowSongNotFoundException() {
                UUID songId = UUID.randomUUID();
                when(songRepoPort.findById(songId)).thenReturn(java.util.Optional.empty());

                assertThrows(SongNotFoundException.class, () -> songService.getSongById(songId));
        }

        @Test
        void getSongs_WhenArtistMissing_ShouldThrowArtistNotFoundException() {
                SongFilterRequest filter = new SongFilterRequest();
                UUID artistId = UUID.randomUUID();
                filter.setArtistId(artistId);

                when(artistRepoPort.existsById(artistId)).thenReturn(false);

                assertThrows(ArtistNotFoundException.class, () -> songService.getSongs(filter));
        }

        @Test
        void getSongs_WhenAlbumMissing_ShouldThrowAlbumNotFoundException() {
                SongFilterRequest filter = new SongFilterRequest();
                UUID albumId = UUID.randomUUID();
                filter.setAlbumId(albumId);

                when(albumRepoPort.existsById(albumId)).thenReturn(false);

                assertThrows(AlbumNotFoundException.class, () -> songService.getSongs(filter));
        }

        @Test
        void getSongs_WhenGenreMissing_ShouldThrowGenreNotFoundException() {
                SongFilterRequest filter = new SongFilterRequest();
                long genreId = 1L;
                filter.setGenreId(genreId);

                when(genreRepoPort.existsById(genreId)).thenReturn(false);

                assertThrows(GenreNotFoundException.class, () -> songService.getSongs(filter));
        }

        @Test
        void deleteSong_WhenValid_ShouldDeleteAndClearRelations() {
                UUID songId = UUID.randomUUID();
                Song song = new Song(
                                songId,
                                "Song",
                                "song",
                                180,
                                "audio-url",
                                "mp3",
                                123L,
                                "cover-url",
                                null,
                                1,
                                LocalDate.now(),
                                0L,
                                true,
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                List.of(),
                                List.of()
                );

                when(songRepoPort.findById(songId)).thenReturn(java.util.Optional.of(song));
                when(storagePort.extractObjectName(eq("ondas-audio"), eq("audio-url"))).thenReturn("song.mp3");
                when(storagePort.extractObjectName(eq("ondas-images"), eq("cover-url"))).thenReturn("cover.jpg");

                songService.deleteSong(songId);

                verify(storagePort).delete("ondas-audio", "song.mp3");
                verify(storagePort).delete("ondas-images", "cover.jpg");
                verify(songArtistRepoPort).replaceSongArtists(songId, List.of());
                verify(songGenreRepoPort).replaceSongGenres(songId, List.of());
                verify(songRepoPort).deleteById(songId);
        }

        @Test
        void deleteSong_WhenNotFound_ShouldThrowSongNotFoundException() {
                UUID songId = UUID.randomUUID();
                when(songRepoPort.findById(songId)).thenReturn(java.util.Optional.empty());

                assertThrows(SongNotFoundException.class, () -> songService.deleteSong(songId));
        }

        @Test
        void streamSong_WhenValidRange_ShouldReturnPartial() {
                UUID songId = UUID.randomUUID();
                Song song = new Song(
                                songId,
                                "Song",
                                "song",
                                180,
                                "audio-url",
                                "mp3",
                                1000L,
                                "cover-url",
                                null,
                                1,
                                LocalDate.now(),
                                0L,
                                true,
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                List.of(),
                                List.of()
                );

                when(songRepoPort.findById(songId)).thenReturn(java.util.Optional.of(song));
                when(storagePort.extractObjectName(eq("ondas-audio"), eq("audio-url"))).thenReturn("song.mp3");
                when(storagePort.getObjectStream(eq("ondas-audio"), eq("song.mp3"), eq(0L), eq(100L)))
                                .thenReturn(new ByteArrayInputStream(new byte[100]));

                SongStreamResponse response = songService.streamSong(songId, "bytes=0-99");

                assertEquals(true, response.isPartial());
                assertEquals(0L, response.rangeStart());
                assertEquals(99L, response.rangeEnd());
        }

        @Test
        void streamSong_WhenInvalidRange_ShouldServeFullWithPartialFlag() {
                UUID songId = UUID.randomUUID();
                Song song = new Song(
                                songId,
                                "Song",
                                "song",
                                180,
                                "audio-url",
                                "mp3",
                                1000L,
                                "cover-url",
                                null,
                                1,
                                LocalDate.now(),
                                0L,
                                true,
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                List.of(),
                                List.of()
                );

                when(songRepoPort.findById(songId)).thenReturn(java.util.Optional.of(song));
                when(storagePort.extractObjectName(eq("ondas-audio"), eq("audio-url"))).thenReturn("song.mp3");
                when(storagePort.getObjectStream(eq("ondas-audio"), eq("song.mp3"), eq(0L), eq(1000L)))
                                .thenReturn(new ByteArrayInputStream(new byte[1000]));

                SongStreamResponse response = songService.streamSong(songId, "bytes=abc-def");

                assertEquals(true, response.isPartial());
                assertEquals(0L, response.rangeStart());
                assertEquals(999L, response.rangeEnd());
        }

        @Test
        void streamSong_WhenInactive_ShouldThrowSongNotFoundException() {
                UUID songId = UUID.randomUUID();
                Song song = new Song(
                                songId,
                                "Song",
                                "song",
                                180,
                                "audio-url",
                                "mp3",
                                1000L,
                                "cover-url",
                                null,
                                1,
                                LocalDate.now(),
                                0L,
                                false,
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now(),
                                List.of(),
                                List.of()
                );

                when(songRepoPort.findById(songId)).thenReturn(java.util.Optional.of(song));

                assertThrows(SongNotFoundException.class, () -> songService.streamSong(songId, null));
        }
}
