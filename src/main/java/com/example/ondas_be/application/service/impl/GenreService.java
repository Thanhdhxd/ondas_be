package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.CreateGenreRequest;
import com.example.ondas_be.application.dto.request.UpdateGenreRequest;
import com.example.ondas_be.application.dto.response.GenreResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.exception.GenreNotFoundException;
import com.example.ondas_be.application.exception.StorageOperationException;
import com.example.ondas_be.application.mapper.GenreMapper;
import com.example.ondas_be.application.service.port.GenreServicePort;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.application.util.SlugUtil;
import com.example.ondas_be.domain.entity.Genre;
import com.example.ondas_be.domain.repoport.GenreRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GenreService implements GenreServicePort {

    private final GenreRepoPort genreRepoPort;
    private final GenreMapper genreMapper;
    private final StoragePort storagePort;

    @Value("${storage.minio.bucket-image}")
    private String imageBucket;

    @Override
    @Transactional
    public GenreResponse createGenre(CreateGenreRequest request, MultipartFile coverFile) {
        String slug = resolveUniqueSlug(resolveSlug(request.getSlug(), request.getName()), null);
        String coverUrl = resolveCoverUrl(request.getCoverUrl(), coverFile);

        Genre genre = new Genre(
                null,
                request.getName().trim(),
                slug,
                request.getDescription(),
                coverUrl,
                LocalDateTime.now());

        return genreMapper.toResponse(genreRepoPort.save(genre));
    }

    @Override
    @Transactional
    public GenreResponse updateGenre(Long id, UpdateGenreRequest request, MultipartFile coverFile) {
        Genre existing = genreRepoPort.findById(id)
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with id: " + id));

        String name = request.getName() != null ? request.getName().trim() : existing.getName();
        String slugCandidate = request.getSlug() != null ? request.getSlug()
                : (request.getName() != null ? name : existing.getSlug());
        String slug = existing.getSlug();
        if (slugCandidate != null && !slugCandidate.equals(existing.getSlug())) {
            slug = resolveUniqueSlug(SlugUtil.toSlug(slugCandidate), existing.getSlug());
        }

        String coverUrl = existing.getCoverUrl();
        if (coverFile != null && !coverFile.isEmpty()) {
            coverUrl = uploadCover(coverFile);
            deleteObject(existing.getCoverUrl());
        } else if (request.getCoverUrl() != null) {
            coverUrl = request.getCoverUrl();
        }

        Genre updated = new Genre(
                existing.getId(),
                name,
                slug,
                request.getDescription() != null ? request.getDescription() : existing.getDescription(),
                coverUrl,
                existing.getCreatedAt());

        return genreMapper.toResponse(genreRepoPort.save(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponse getGenreById(Long id) {
        Genre genre = genreRepoPort.findById(id)
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with id: " + id));
        return genreMapper.toResponse(genre);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {
        return genreMapper.toResponseList(genreRepoPort.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<GenreResponse> searchGenresByName(String query, String mode, int page, int size) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query is required");
        }
        String normalizedMode = mode == null ? "contains" : mode.trim().toLowerCase();
        List<Genre> genres;
        long total;
        if ("fulltext".equals(normalizedMode)) {
            genres = genreRepoPort.findByNameFullText(query, page, size);
            total = genreRepoPort.countByNameFullText(query);
        } else {
            genres = genreRepoPort.findByNameContains(query, page, size);
            total = genreRepoPort.countByNameContains(query);
        }
        List<GenreResponse> items = genreMapper.toResponseList(genres);
        return buildPageResult(items, page, size, total);
    }

    @Override
    @Transactional
    public void deleteGenre(Long id) {
        Genre genre = genreRepoPort.findById(id)
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with id: " + id));
        deleteObject(genre.getCoverUrl());
        genreRepoPort.deleteById(id);
    }

    private String resolveCoverUrl(String coverUrl, MultipartFile coverFile) {
        if (coverFile != null && !coverFile.isEmpty()) {
            return uploadCover(coverFile);
        }
        return coverUrl;
    }

    private String uploadCover(MultipartFile file) {
        String objectName = "genres/cover/" + UUID.randomUUID() + resolveExtension(file.getOriginalFilename());
        try {
            return storagePort.upload(imageBucket, objectName, file.getInputStream(), file.getSize(),
                    file.getContentType());
        } catch (IOException ex) {
            throw new StorageOperationException("Cannot read upload stream", ex);
        }
    }

    private void deleteObject(String url) {
        String objectName = storagePort.extractObjectName(imageBucket, url);
        storagePort.delete(imageBucket, objectName);
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
    }

    private String resolveSlug(String slug, String name) {
        if (slug != null && !slug.isBlank()) {
            return SlugUtil.toSlug(slug);
        }
        return SlugUtil.toSlug(name);
    }

    private String resolveUniqueSlug(String slugCandidate, String currentSlug) {
        if (slugCandidate == null) {
            return currentSlug;
        }
        if (currentSlug != null && currentSlug.equals(slugCandidate)) {
            return currentSlug;
        }
        if (!genreRepoPort.existsBySlug(slugCandidate)) {
            return slugCandidate;
        }
        return slugCandidate + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private PageResultDto<GenreResponse> buildPageResult(List<GenreResponse> items, int page, int size, long total) {
        int safeSize = Math.max(1, size);
        int totalPages = (int) Math.ceil((double) total / safeSize);
        return PageResultDto.<GenreResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }
}
