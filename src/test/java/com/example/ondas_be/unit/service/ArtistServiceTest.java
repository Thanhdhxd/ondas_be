package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.request.CreateArtistRequest;
import com.example.ondas_be.application.dto.request.ArtistFilterRequest;
import com.example.ondas_be.application.dto.request.UpdateArtistRequest;
import com.example.ondas_be.application.dto.response.ArtistResponse;
import com.example.ondas_be.application.exception.ArtistNotFoundException;
import com.example.ondas_be.application.mapper.ArtistMapper;
import com.example.ondas_be.application.service.impl.ArtistService;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.domain.entity.Artist;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
class ArtistServiceTest {

    @Mock
    private ArtistRepoPort artistRepoPort;

    @Mock
    private StoragePort storagePort;

    @Mock
    private ArtistMapper artistMapper;

    @InjectMocks
    private ArtistService artistService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(artistService, "imageBucket", "ondas-images");
    }

    @Test
    void createArtist_WhenAvatarProvided_ShouldUploadAndSave() {
        CreateArtistRequest request = new CreateArtistRequest();
        request.setName("Artist Name");
        request.setSlug("artist-name");

        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "avatar.jpg",
                "image/jpeg",
                "avatar".getBytes());

        when(artistRepoPort.existsBySlug(any())).thenReturn(false);
        when(storagePort.upload(eq("ondas-images"), any(), any(), anyLong(), any()))
                .thenReturn("avatar-url");
        when(artistRepoPort.save(any(Artist.class))).thenAnswer(invocation -> {
            Artist input = invocation.getArgument(0);
            return new Artist(UUID.randomUUID(), input.getName(), input.getSlug(), input.getBio(),
                    input.getAvatarUrl(), input.getCountry(), input.getCreatedBy(),
                    LocalDateTime.now(), LocalDateTime.now());
        });
        when(artistMapper.toResponse(any(Artist.class))).thenReturn(new ArtistResponse());

        artistService.createArtist(request, avatar);

        verify(storagePort).upload(eq("ondas-images"), any(), any(), anyLong(), any());
        verify(artistRepoPort).save(any(Artist.class));
    }

        @Test
        void createArtist_WhenAvatarMissing_ShouldSaveWithoutUpload() {
                CreateArtistRequest request = new CreateArtistRequest();
                request.setName("Artist Name");
                request.setSlug("artist-name");

                when(artistRepoPort.existsBySlug(any())).thenReturn(false);
                when(artistRepoPort.save(any(Artist.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(artistMapper.toResponse(any(Artist.class))).thenReturn(new ArtistResponse());

                artistService.createArtist(request, null);

                verify(storagePort, never()).upload(any(), any(), any(), anyLong(), any());
                verify(artistRepoPort).save(any(Artist.class));
        }

    @Test
    void updateArtist_WhenAvatarProvided_ShouldDeleteOldAvatar() {
        UUID artistId = UUID.randomUUID();
        Artist existing = new Artist(
                artistId,
                "Old Artist",
                "old-artist",
                null,
                "old-avatar-url",
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        UpdateArtistRequest request = new UpdateArtistRequest();
        request.setName("New Artist");

        MockMultipartFile avatar = new MockMultipartFile(
                "avatar",
                "new.jpg",
                "image/jpeg",
                "new".getBytes());

        when(artistRepoPort.findById(artistId)).thenReturn(java.util.Optional.of(existing));
        when(artistRepoPort.existsBySlug(any())).thenReturn(false);
        when(storagePort.upload(eq("ondas-images"), any(), any(), anyLong(), any()))
                .thenReturn("new-avatar-url");
        when(storagePort.extractObjectName(eq("ondas-images"), eq("old-avatar-url"))).thenReturn("old.jpg");
        when(artistRepoPort.save(any(Artist.class))).thenReturn(existing);
        when(artistMapper.toResponse(any(Artist.class))).thenReturn(new ArtistResponse());

        ArtistResponse response = artistService.updateArtist(artistId, request, avatar);

        assertEquals(response.getClass(), ArtistResponse.class);
        verify(storagePort).delete("ondas-images", "old.jpg");
    }

        @Test
        void updateArtist_WhenNotFound_ShouldThrowArtistNotFoundException() {
                UUID artistId = UUID.randomUUID();
                UpdateArtistRequest request = new UpdateArtistRequest();

                when(artistRepoPort.findById(artistId)).thenReturn(Optional.empty());

                assertThrows(ArtistNotFoundException.class,
                                () -> artistService.updateArtist(artistId, request, null));
        }

        @Test
        void getArtistById_WhenValid_ShouldReturnResponse() {
                UUID artistId = UUID.randomUUID();
                Artist artist = new Artist(
                                artistId,
                                "Artist",
                                "artist",
                                null,
                                null,
                                null,
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                );

                when(artistRepoPort.findById(artistId)).thenReturn(Optional.of(artist));
                when(artistMapper.toResponse(artist)).thenReturn(new ArtistResponse());

                ArtistResponse response = artistService.getArtistById(artistId);

                assertEquals(ArtistResponse.class, response.getClass());
        }

        @Test
        void getArtists_WhenQueryProvided_ShouldReturnPageResult() {
                ArtistFilterRequest filter = new ArtistFilterRequest();
                filter.setQuery("art");
                filter.setPage(0);
                filter.setSize(10);

                when(artistRepoPort.findByNameContains("art", 0, 10)).thenReturn(List.of());
                when(artistRepoPort.countByNameContains("art")).thenReturn(0L);
                when(artistMapper.toResponseList(List.of())).thenReturn(List.of());

                assertEquals(0, artistService.getArtists(filter).getItems().size());
        }

        @Test
        void deleteArtist_WhenValid_ShouldDelete() {
                UUID artistId = UUID.randomUUID();
                Artist artist = new Artist(
                                artistId,
                                "Artist",
                                "artist",
                                null,
                                "avatar-url",
                                null,
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                );

                when(artistRepoPort.findById(artistId)).thenReturn(Optional.of(artist));
                when(storagePort.extractObjectName(eq("ondas-images"), eq("avatar-url"))).thenReturn("avatar.jpg");

                artistService.deleteArtist(artistId);

                verify(storagePort).delete("ondas-images", "avatar.jpg");
                verify(artistRepoPort).deleteById(artistId);
        }

        @Test
        void deleteArtist_WhenNotFound_ShouldThrowArtistNotFoundException() {
                UUID artistId = UUID.randomUUID();
                when(artistRepoPort.findById(artistId)).thenReturn(Optional.empty());

                assertThrows(ArtistNotFoundException.class, () -> artistService.deleteArtist(artistId));
        }
}
