package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.request.CreateArtistRequest;
import com.example.ondas_be.application.dto.request.ArtistFilterRequest;
import com.example.ondas_be.application.dto.request.UpdateArtistRequest;
import com.example.ondas_be.application.dto.response.ArtistResponse;
import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.exception.ArtistNotFoundException;
import com.example.ondas_be.application.exception.StorageOperationException;
import com.example.ondas_be.application.mapper.ArtistMapper;
import com.example.ondas_be.application.service.port.ArtistServicePort;
import com.example.ondas_be.application.service.port.StoragePort;
import com.example.ondas_be.application.util.SlugUtil;
import com.example.ondas_be.domain.entity.Artist;
import com.example.ondas_be.domain.repoport.ArtistRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistService implements ArtistServicePort {

    private final ArtistRepoPort artistRepoPort;
    private final StoragePort storagePort;
    private final ArtistMapper artistMapper;

    @Value("${storage.minio.bucket-image}")
    private String imageBucket;

    @Override
    @Transactional
    public ArtistResponse createArtist(CreateArtistRequest request, MultipartFile avatarFile) {
        String slug = resolveUniqueSlug(resolveSlug(request.getSlug(), request.getName()), null);
        String avatarUrl = uploadOptionalImage(avatarFile, "artists/avatar/");

        Artist artist = new Artist(
                null,
                request.getName().trim(),
                slug,
                request.getBio(),
                avatarUrl,
                request.getCountry(),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Artist saved = artistRepoPort.save(artist);
        return artistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ArtistResponse updateArtist(UUID id, UpdateArtistRequest request, MultipartFile avatarFile) {
        Artist existing = artistRepoPort.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Artist not found with id: " + id));

        String name = request.getName() != null ? request.getName().trim() : existing.getName();
        String slugCandidate = request.getSlug() != null ? request.getSlug() : (request.getName() != null ? name : existing.getSlug());
        String slug = existing.getSlug();
        if (!Objects.equals(existing.getSlug(), slugCandidate) && slugCandidate != null) {
            slug = resolveUniqueSlug(SlugUtil.toSlug(slugCandidate), existing.getSlug());
        }

        String avatarUrl = existing.getAvatarUrl();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            avatarUrl = uploadOptionalImage(avatarFile, "artists/avatar/");
            deleteObject(existing.getAvatarUrl());
        }

        Artist updated = new Artist(
                existing.getId(),
                name,
                slug,
                request.getBio() != null ? request.getBio() : existing.getBio(),
                avatarUrl,
                request.getCountry() != null ? request.getCountry() : existing.getCountry(),
                existing.getCreatedBy(),
                existing.getCreatedAt(),
                existing.getUpdatedAt()
        );

        return artistMapper.toResponse(artistRepoPort.save(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public ArtistResponse getArtistById(UUID id) {
        Artist artist = artistRepoPort.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Artist not found with id: " + id));
        return artistMapper.toResponse(artist);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<ArtistResponse> getArtists(ArtistFilterRequest filter) {
        int page = filter.getPage();
        int size = filter.getSize();
        String normalizedMode = filter.getMode() == null ? "contains" : filter.getMode().trim().toLowerCase();

        List<Artist> artists;
        long total;

        if (filter.getQuery() != null && !filter.getQuery().isBlank()) {
            if ("fulltext".equals(normalizedMode)) {
                artists = artistRepoPort.findByNameFullText(filter.getQuery(), page, size);
                total = artistRepoPort.countByNameFullText(filter.getQuery());
            } else {
                artists = artistRepoPort.findByNameContains(filter.getQuery(), page, size);
                total = artistRepoPort.countByNameContains(filter.getQuery());
            }
        } else {
            artists = artistRepoPort.findAll(page, size);
            total = artistRepoPort.countAll();
        }

        List<ArtistResponse> items = artistMapper.toResponseList(artists);
        return buildPageResult(items, page, size, total);
    }

    @Override
    @Transactional
    public void deleteArtist(UUID id) {
        Artist artist = artistRepoPort.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Artist not found with id: " + id));

        deleteObject(artist.getAvatarUrl());
        artistRepoPort.deleteById(id);
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
        if (!artistRepoPort.existsBySlug(slugCandidate)) {
            return slugCandidate;
        }
        return slugCandidate + "-" + UUID.randomUUID().toString().substring(0, 8);
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
        String objectName = storagePort.extractObjectName(imageBucket, url);
        storagePort.delete(imageBucket, objectName);
    }

    private PageResultDto<ArtistResponse> buildPageResult(List<ArtistResponse> items, int page, int size, long total) {
        int safeSize = Math.max(1, size);
        int totalPages = (int) Math.ceil((double) total / safeSize);
        return PageResultDto.<ArtistResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
    }
}
