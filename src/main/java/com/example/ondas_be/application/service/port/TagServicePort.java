package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.CreateTagRequest;
import com.example.ondas_be.application.dto.request.UpdateTagRequest;
import com.example.ondas_be.application.dto.response.TagResponse;

import java.util.List;

public interface TagServicePort {

    TagResponse createTag(CreateTagRequest request);

    TagResponse updateTag(Long id, UpdateTagRequest request);

    TagResponse getTagById(Long id);

    List<TagResponse> getAllTags(String type);

    PageResultDto<TagResponse> searchTagsByName(String query, String mode, int page, int size);

    void deleteTag(Long id);
}
