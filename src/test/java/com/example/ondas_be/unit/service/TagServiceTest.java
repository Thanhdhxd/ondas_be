package com.example.ondas_be.unit.service;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.CreateTagRequest;
import com.example.ondas_be.application.dto.request.UpdateTagRequest;
import com.example.ondas_be.application.dto.response.TagResponse;
import com.example.ondas_be.application.exception.TagNotFoundException;
import com.example.ondas_be.application.mapper.TagMapper;
import com.example.ondas_be.application.service.impl.TagService;
import com.example.ondas_be.domain.entity.Tag;
import com.example.ondas_be.domain.repoport.TagRepoPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepoPort tagRepoPort;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagService tagService;

    @Test
    void createTag_WhenDuplicateName_ShouldThrow() {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Happy");

        when(tagRepoPort.existsByName("Happy")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> tagService.createTag(request));
    }

    @Test
    void createTag_WhenValid_ShouldDefaultTypeAndNormalizeColor() {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Happy");
        request.setColorHex("#ff9900");

        when(tagRepoPort.existsByName("Happy")).thenReturn(false);
        when(tagRepoPort.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tagMapper.toResponse(any(Tag.class))).thenReturn(new TagResponse());

        tagService.createTag(request);

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagRepoPort).save(captor.capture());
        Tag saved = captor.getValue();
        assertEquals("mood", saved.getType());
        assertEquals("#FF9900", saved.getColorHex());
    }

    @Test
    void updateTag_WhenNotFound_ShouldThrowTagNotFound() {
        when(tagRepoPort.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TagNotFoundException.class, () -> tagService.updateTag(1L, new UpdateTagRequest()));
    }

    @Test
    void updateTag_WhenInvalidType_ShouldThrow() {
        Tag existing = new Tag(1L, "Happy", "mood", null, LocalDateTime.now());
        when(tagRepoPort.findById(1L)).thenReturn(Optional.of(existing));

        UpdateTagRequest request = new UpdateTagRequest();
        request.setType("invalid");

        assertThrows(IllegalArgumentException.class, () -> tagService.updateTag(1L, request));
    }

    @Test
    void getAllTags_WhenTypeProvided_ShouldFilter() {
        when(tagRepoPort.findByType("mood")).thenReturn(List.of());
        when(tagMapper.toResponseList(List.of())).thenReturn(List.of());

        List<TagResponse> result = tagService.getAllTags("mood");

        assertEquals(0, result.size());
    }

    @Test
    void searchTags_WhenBlankQuery_ShouldThrow() {
        assertThrows(IllegalArgumentException.class, () -> tagService.searchTagsByName(" ", null, 0, 10));
    }

    @Test
    void searchTags_WhenValid_ShouldReturnPage() {
        when(tagRepoPort.findByNameContains("happy", 0, 10)).thenReturn(List.of());
        when(tagRepoPort.countByNameContains("happy")).thenReturn(0L);
        when(tagMapper.toResponseList(List.of())).thenReturn(List.of());

        PageResultDto<TagResponse> result = tagService.searchTagsByName("happy", "contains", 0, 10);

        assertEquals(0, result.getItems().size());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    void deleteTag_WhenNotFound_ShouldThrow() {
        when(tagRepoPort.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TagNotFoundException.class, () -> tagService.deleteTag(1L));
    }

    @Test
    void deleteTag_WhenValid_ShouldDelete() {
        Tag existing = new Tag(1L, "Happy", "mood", null, LocalDateTime.now());
        when(tagRepoPort.findById(1L)).thenReturn(Optional.of(existing));

        tagService.deleteTag(1L);

        verify(tagRepoPort).deleteById(1L);
    }
}
