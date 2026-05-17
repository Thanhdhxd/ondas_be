package com.example.ondas_be.application.service.impl;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.CreateTagRequest;
import com.example.ondas_be.application.dto.request.UpdateTagRequest;
import com.example.ondas_be.application.dto.response.TagResponse;
import com.example.ondas_be.application.exception.TagNotFoundException;
import com.example.ondas_be.application.mapper.TagMapper;
import com.example.ondas_be.application.service.port.TagServicePort;
import com.example.ondas_be.domain.entity.Tag;
import com.example.ondas_be.domain.repoport.TagRepoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TagService implements TagServicePort {

    private static final Set<String> ALLOWED_TYPES = Set.of("mood", "theme", "activity", "era");

    private final TagRepoPort tagRepoPort;
    private final TagMapper tagMapper;

    @Override
    @Transactional
    public TagResponse createTag(CreateTagRequest request) {
        String name = request.getName().trim();
        if (tagRepoPort.existsByName(name)) {
            throw new IllegalArgumentException("Tag name already exists");
        }

        String type = resolveType(request.getType(), true);
        String colorHex = normalizeColor(request.getColorHex());

        Tag tag = new Tag(
                null,
                name,
                type,
                colorHex,
                LocalDateTime.now());

        return tagMapper.toResponse(tagRepoPort.save(tag));
    }

    @Override
    @Transactional
    public TagResponse updateTag(Long id, UpdateTagRequest request) {
        Tag existing = tagRepoPort.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + id));

        String name = existing.getName();
        if (request.getName() != null) {
            String trimmed = request.getName().trim();
            if (trimmed.isBlank()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (!trimmed.equals(existing.getName()) && tagRepoPort.existsByName(trimmed)) {
                throw new IllegalArgumentException("Tag name already exists");
            }
            name = trimmed;
        }

        String type = request.getType() != null
                ? resolveType(request.getType(), false)
                : existing.getType();

        String colorHex = request.getColorHex() != null
                ? normalizeColor(request.getColorHex())
                : existing.getColorHex();

        Tag updated = new Tag(
                existing.getId(),
                name,
                type,
                colorHex,
                existing.getCreatedAt());

        return tagMapper.toResponse(tagRepoPort.save(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse getTagById(Long id) {
        Tag tag = tagRepoPort.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + id));
        return tagMapper.toResponse(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags(String type) {
        if (type == null || type.isBlank()) {
            return tagMapper.toResponseList(tagRepoPort.findAll());
        }
        String normalizedType = resolveType(type, false);
        return tagMapper.toResponseList(tagRepoPort.findByType(normalizedType));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResultDto<TagResponse> searchTagsByName(String query, String mode, int page, int size) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query is required");
        }
        String normalizedMode = mode == null ? "contains" : mode.trim().toLowerCase();
        List<Tag> tags;
        long total;
        if ("fulltext".equals(normalizedMode)) {
            tags = tagRepoPort.findByNameFullText(query, page, size);
            total = tagRepoPort.countByNameFullText(query);
        } else {
            tags = tagRepoPort.findByNameContains(query, page, size);
            total = tagRepoPort.countByNameContains(query);
        }
        List<TagResponse> items = tagMapper.toResponseList(tags);
        return buildPageResult(items, page, size, total);
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepoPort.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Tag not found with id: " + id));
        tagRepoPort.deleteById(tag.getId());
    }

    private String resolveType(String type, boolean defaultToMood) {
        if (type == null || type.isBlank()) {
            if (defaultToMood) {
                return "mood";
            }
            throw new IllegalArgumentException("Type is required");
        }
        String normalized = type.trim().toLowerCase();
        if (!ALLOWED_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("Invalid tag type: " + type);
        }
        return normalized;
    }

    private String normalizeColor(String colorHex) {
        if (colorHex == null) {
            return null;
        }
        return colorHex.toUpperCase();
    }

    private PageResultDto<TagResponse> buildPageResult(List<TagResponse> items, int page, int size, long total) {
        int safeSize = Math.max(1, size);
        int totalPages = (int) Math.ceil((double) total / safeSize);
        return PageResultDto.<TagResponse>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }
}
