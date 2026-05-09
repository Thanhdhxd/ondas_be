package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.CreateGenreRequest;
import com.example.ondas_be.application.dto.request.UpdateGenreRequest;
import com.example.ondas_be.application.dto.response.GenreResponse;
import com.example.ondas_be.application.exception.GenreNotFoundException;
import com.example.ondas_be.application.mapper.GenreMapper;
import com.example.ondas_be.application.service.impl.GenreService;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.domain.entity.Genre;
import com.example.ondas_be.domain.repoport.GenreRepoPort;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepoPort genreRepoPort;

    @Mock
    private GenreMapper genreMapper;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private GenreService genreService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(genreService, "imageBucket", "ondas-images");
    }

    @Test
    void createGenre_WhenCoverFileProvided_ShouldUploadAndSave() {
        CreateGenreRequest request = new CreateGenreRequest();
        request.setName("Pop");
        request.setSlug("pop");

        MockMultipartFile cover = new MockMultipartFile(
                "cover",
                "cover.jpg",
                "image/jpeg",
                "cover".getBytes()
        );

        when(storagePort.upload(eq("ondas-images"), any(), any(), anyLong(), any()))
                .thenReturn("cover-url");
        when(genreRepoPort.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(genreMapper.toResponse(any(Genre.class))).thenReturn(new GenreResponse());

        genreService.createGenre(request, cover);

        verify(storagePort).upload(eq("ondas-images"), any(), any(), anyLong(), any());
        verify(genreRepoPort).save(any(Genre.class));
    }

    @Test
    void createGenre_WhenCoverUrlProvided_ShouldSkipUpload() {
        CreateGenreRequest request = new CreateGenreRequest();
        request.setName("Pop");
        request.setSlug("pop");
        request.setCoverUrl("existing-url");

        when(genreRepoPort.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(genreMapper.toResponse(any(Genre.class))).thenReturn(new GenreResponse());

        genreService.createGenre(request, null);

        verify(storagePort, never()).upload(any(), any(), any(), anyLong(), any());
    }

    @Test
    void updateGenre_WhenCoverFileProvided_ShouldUploadAndDeleteOld() {
        long genreId = 1L;
        Genre existing = new Genre(genreId, "Pop", "pop", null, "old-url", LocalDateTime.now());

        UpdateGenreRequest request = new UpdateGenreRequest();
        request.setName("Pop New");

        MockMultipartFile cover = new MockMultipartFile(
                "cover",
                "cover.jpg",
                "image/jpeg",
                "cover".getBytes()
        );

        when(genreRepoPort.findById(genreId)).thenReturn(Optional.of(existing));
        when(storagePort.upload(eq("ondas-images"), any(), any(), anyLong(), any()))
                .thenReturn("new-url");
        when(storagePort.extractObjectName(eq("ondas-images"), eq("old-url"))).thenReturn("old.jpg");
        when(genreRepoPort.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(genreMapper.toResponse(any(Genre.class))).thenReturn(new GenreResponse());

        genreService.updateGenre(genreId, request, cover);

        verify(storagePort).delete("ondas-images", "old.jpg");
    }

    @Test
    void updateGenre_WhenCoverUrlProvided_ShouldUseNewUrlWithoutDeleting() {
        long genreId = 1L;
        Genre existing = new Genre(genreId, "Pop", "pop", null, "old-url", LocalDateTime.now());

        UpdateGenreRequest request = new UpdateGenreRequest();
        request.setCoverUrl("new-url");

        when(genreRepoPort.findById(genreId)).thenReturn(Optional.of(existing));
        when(genreRepoPort.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(genreMapper.toResponse(any(Genre.class))).thenReturn(new GenreResponse());

        genreService.updateGenre(genreId, request, null);

        verify(storagePort, never()).delete(any(), any());
    }

    @Test
    void getGenreById_WhenNotFound_ShouldThrowGenreNotFoundException() {
        when(genreRepoPort.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GenreNotFoundException.class, () -> genreService.getGenreById(1L));
    }

    @Test
    void getAllGenres_ShouldReturnList() {
        when(genreRepoPort.findAll()).thenReturn(List.of());
        when(genreMapper.toResponseList(List.of())).thenReturn(List.of());

        List<GenreResponse> result = genreService.getAllGenres();

        assertEquals(0, result.size());
    }

    @Test
    void searchGenresByName_WhenBlankQuery_ShouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> genreService.searchGenresByName("  ", null, 0, 10));
    }

    @Test
    void searchGenresByName_WhenValid_ShouldReturnPage() {
        when(genreRepoPort.findByNameContains("pop", 0, 10)).thenReturn(List.of());
        when(genreRepoPort.countByNameContains("pop")).thenReturn(0L);
        when(genreMapper.toResponseList(List.of())).thenReturn(List.of());

        PageResultDto<GenreResponse> result = genreService.searchGenresByName("pop", "contains", 0, 10);

        assertEquals(0, result.getItems().size());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    void deleteGenre_WhenNotFound_ShouldThrowGenreNotFoundException() {
        when(genreRepoPort.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GenreNotFoundException.class, () -> genreService.deleteGenre(1L));
    }

    @Test
    void deleteGenre_WhenValid_ShouldDelete() {
        Genre existing = new Genre(1L, "Pop", "pop", null, "cover-url", LocalDateTime.now());

        when(genreRepoPort.findById(1L)).thenReturn(Optional.of(existing));
        when(storagePort.extractObjectName(eq("ondas-images"), eq("cover-url"))).thenReturn("cover.jpg");

        genreService.deleteGenre(1L);

        verify(storagePort).delete("ondas-images", "cover.jpg");
        verify(genreRepoPort).deleteById(1L);
    }
}
