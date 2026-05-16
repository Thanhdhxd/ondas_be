package com.example.ondas_be.integration.controller;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.CreateTagRequest;
import com.example.ondas_be.application.dto.request.UpdateTagRequest;
import com.example.ondas_be.application.dto.response.TagResponse;
import com.example.ondas_be.application.exception.TagNotFoundException;
import com.example.ondas_be.application.service.port.TagServicePort;
import com.example.ondas_be.infrastructure.security.JwtUtil;
import com.example.ondas_be.infrastructure.security.SecurityConfig;
import com.example.ondas_be.infrastructure.security.UserDetailsServiceImpl;
import com.example.ondas_be.presentation.advice.GlobalExceptionHandler;
import com.example.ondas_be.presentation.controller.TagController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

@WebMvcTest(controllers = TagController.class)
@AutoConfigureMockMvc
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TagServicePort tagServicePort;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private TagResponse buildTagResponse(Long id) {
        return TagResponse.builder()
                .id(id)
                .name("Happy")
                .type("mood")
                .colorHex("#FF9900")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTag_ShouldReturn201_WhenValid() throws Exception {
        when(tagServicePort.createTag(any(CreateTagRequest.class))).thenReturn(buildTagResponse(1L));

        CreateTagRequest request = new CreateTagRequest();
        request.setName("Happy");
        request.setType("mood");
        request.setColorHex("#FF9900");

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Happy"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTag_ShouldReturn400_WhenNameBlank() throws Exception {
        CreateTagRequest request = new CreateTagRequest();
        request.setName(" ");

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTag_ShouldReturn400_WhenColorInvalid() throws Exception {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Happy");
        request.setColorHex("#ZZZZZZ");

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createTag_ShouldReturn403_WhenRoleNotAllowed() throws Exception {
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Happy");

        mockMvc.perform(post("/api/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTag_ShouldReturn200_WhenValid() throws Exception {
        when(tagServicePort.updateTag(eq(1L), any(UpdateTagRequest.class)))
                .thenReturn(buildTagResponse(1L));

        UpdateTagRequest request = new UpdateTagRequest();
        request.setName("Calm");

        mockMvc.perform(put("/api/tags/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTag_ShouldReturn404_WhenNotFound() throws Exception {
        when(tagServicePort.updateTag(eq(1L), any(UpdateTagRequest.class)))
                .thenThrow(new TagNotFoundException("Tag not found"));

        UpdateTagRequest request = new UpdateTagRequest();
        request.setName("Calm");

        mockMvc.perform(put("/api/tags/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTagById_ShouldReturn200_WhenExists() throws Exception {
        when(tagServicePort.getTagById(1L)).thenReturn(buildTagResponse(1L));

        mockMvc.perform(get("/api/tags/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTagById_ShouldReturn404_WhenNotFound() throws Exception {
        when(tagServicePort.getTagById(1L)).thenThrow(new TagNotFoundException("Tag not found"));

        mockMvc.perform(get("/api/tags/{id}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllTags_ShouldReturn200_WhenValid() throws Exception {
        when(tagServicePort.getAllTags(null)).thenReturn(List.of(buildTagResponse(1L)));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchTags_ShouldReturn200_WhenQueryValid() throws Exception {
        PageResultDto<TagResponse> page = PageResultDto.<TagResponse>builder()
                .items(List.of(buildTagResponse(1L)))
                .page(0)
                .size(20)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(tagServicePort.searchTagsByName(eq("happy"), eq("contains"), eq(0), eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/api/tags/search")
                        .param("query", "happy")
                        .param("mode", "contains"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchTags_ShouldReturn400_WhenQueryMissing() throws Exception {
        mockMvc.perform(get("/api/tags/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTag_ShouldReturn200_WhenValid() throws Exception {
        doNothing().when(tagServicePort).deleteTag(1L);

        mockMvc.perform(delete("/api/tags/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTag_ShouldReturn404_WhenNotFound() throws Exception {
        doThrow(new TagNotFoundException("Tag not found")).when(tagServicePort).deleteTag(1L);

        mockMvc.perform(delete("/api/tags/{id}", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
