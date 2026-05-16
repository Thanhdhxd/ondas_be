package com.example.ondas_be.application.mapper;

import com.example.ondas_be.application.dto.response.TagResponse;
import com.example.ondas_be.domain.entity.Tag;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagResponse toResponse(Tag tag);

    List<TagResponse> toResponseList(List<Tag> tags);
}
