package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.SearchFilterRequest;
import com.example.ondas_be.application.dto.response.AlbumResponse;
import com.example.ondas_be.application.dto.response.ArtistResponse;
import com.example.ondas_be.application.dto.response.ArtistSummaryResponse;
import com.example.ondas_be.application.dto.response.GenreSummaryResponse;
import com.example.ondas_be.application.dto.response.SearchResponse;
import com.example.ondas_be.application.dto.response.SongResponse;
import com.example.ondas_be.application.mapper.AlbumMapper;
import com.example.ondas_be.application.mapper.ArtistMapper;
import com.example.ondas_be.application.mapper.GenreMapper;
import com.example.ondas_be.application.mapper.SongMapper;
import com.example.ondas_be.application.service.impl.SearchService;
import com.example.ondas_be.domain.entity.Album;
import com.example.ondas_be.domain.entity.Artist;
import com.example.ondas_be.domain.entity.Genre;
import com.example.ondas_be.domain.entity.Song;
import com.example.ondas_be.domain.repoport.AlbumArtistRepoPort;
import com.example.ondas_be.domain.repoport.AlbumRepoPort;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import com.example.ondas_be.domain.repoport.GenreRepoPort;
import com.example.ondas_be.domain.repoport.SongArtistRepoPort;
import com.example.ondas_be.domain.repoport.SongGenreRepoPort;
import com.example.ondas_be.domain.repoport.SongRepoPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

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
    private AlbumArtistRepoPort albumArtistRepoPort;

    @Mock
    private SongMapper songMapper;

    @Mock
    private ArtistMapper artistMapper;

    @Mock
    private AlbumMapper albumMapper;

    @Mock
    private GenreMapper genreMapper;

    @InjectMocks
    private SearchService searchService;

    @Test
    void search_WhenValidQuery_ShouldReturnAggregatedResult() {
        UUID songId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        UUID albumId = UUID.randomUUID();
        long genreId = 1L;

        SearchFilterRequest filter = new SearchFilterRequest();
        filter.setQuery("love");
        filter.setPage(0);
        filter.setSize(10);

        Song song = new Song(
                songId,
                "Love Song",
                "love-song",
                200,
                "audio-url",
                "mp3",
                1000L,
                "cover-url",
                albumId,
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
        Artist artist = new Artist(
                artistId,
                "Artist Name",
                "artist-name",
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Album album = new Album(
                albumId,
                "Love Album",
                "love-album",
                null,
                null,
                "album",
                null,
                10,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(artistId)
        );
        Genre genre = new Genre(genreId, "Pop", "pop", null, null, LocalDateTime.now());

        when(songRepoPort.findByTitleContains("love", 0, 10)).thenReturn(List.of(song));
        when(songRepoPort.countByTitleContains("love")).thenReturn(1L);
        when(artistRepoPort.findByNameContains("love", 0, 10)).thenReturn(List.of(artist));
        when(artistRepoPort.countByNameContains("love")).thenReturn(1L);
        when(albumRepoPort.findByTitleContains("love", 0, 10)).thenReturn(List.of(album));
        when(albumRepoPort.countByTitleContains("love")).thenReturn(1L);
        when(songArtistRepoPort.findArtistIdsBySongIds(List.of(songId)))
                .thenReturn(Map.of(songId, List.of(artistId)));
        when(songGenreRepoPort.findGenreIdsBySongIds(List.of(songId)))
                .thenReturn(Map.of(songId, List.of(genreId)));
        when(artistRepoPort.findByIds(List.of(artistId))).thenReturn(List.of(artist));
        when(genreRepoPort.findByIds(List.of(genreId))).thenReturn(List.of(genre));
        when(songMapper.toResponse(song)).thenReturn(SongResponse.builder().id(songId).title("Love Song").build());
        when(artistMapper.toSummaryResponse(artist))
                .thenReturn(ArtistSummaryResponse.builder().id(artistId).name("Artist Name").build());
        when(genreMapper.toSummaryResponse(genre))
                .thenReturn(GenreSummaryResponse.builder().id(genreId).name("Pop").build());
        when(artistMapper.toResponseList(List.of(artist)))
                .thenReturn(List.of(ArtistResponse.builder().id(artistId).name("Artist Name").build()));
        when(albumMapper.toResponse(album))
                .thenReturn(AlbumResponse.builder().id(albumId).title("Love Album").build());
        when(albumArtistRepoPort.findArtistIdsByAlbumId(albumId)).thenReturn(List.of(artistId));

        SearchResponse response = searchService.search(filter);

        assertEquals("love", response.getQuery());
        assertEquals(1L, response.getTotalSongs());
        assertEquals(1L, response.getTotalArtists());
        assertEquals(1L, response.getTotalAlbums());
        assertEquals(1, response.getSongs().size());
        assertEquals(1, response.getArtists().size());
        assertEquals(1, response.getAlbums().size());
        assertEquals(1, response.getSongs().get(0).getArtists().size());
        assertEquals(1, response.getSongs().get(0).getGenres().size());
        assertEquals(List.of(artistId), response.getAlbums().get(0).getArtistIds());
        assertEquals(List.of(), response.getAlbums().get(0).getTracklist());
    }

    @Test
    void search_WhenQueryBlank_ShouldThrowIllegalArgumentException() {
        SearchFilterRequest filter = new SearchFilterRequest();
        filter.setQuery("   ");

        assertThrows(IllegalArgumentException.class, () -> searchService.search(filter));
        verifyNoInteractions(songRepoPort, artistRepoPort, albumRepoPort, songMapper, artistMapper, albumMapper);
    }
}
