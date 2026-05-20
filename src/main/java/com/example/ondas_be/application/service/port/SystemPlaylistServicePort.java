package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AddSongToSystemPlaylistRequest;
import com.example.ondas_be.application.dto.request.CreateSystemPlaylistRequest;
import com.example.ondas_be.application.dto.request.ReorderSystemPlaylistSongsRequest;
import com.example.ondas_be.application.dto.request.SystemPlaylistFilterRequest;
import com.example.ondas_be.application.dto.request.UpdateSystemPlaylistRequest;
import com.example.ondas_be.application.dto.response.SystemPlaylistResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface SystemPlaylistServicePort {

    SystemPlaylistResponse createSystemPlaylist(String email, CreateSystemPlaylistRequest request, MultipartFile coverFile);

    SystemPlaylistResponse updateSystemPlaylist(String email, UUID id, UpdateSystemPlaylistRequest request, MultipartFile coverFile);

    SystemPlaylistResponse getSystemPlaylistById(UUID id);

    SystemPlaylistResponse getActiveSystemPlaylistById(UUID id);

    PageResultDto<SystemPlaylistResponse> getSystemPlaylists(SystemPlaylistFilterRequest filter);

    PageResultDto<SystemPlaylistResponse> getActiveSystemPlaylists(SystemPlaylistFilterRequest filter);

    void deleteSystemPlaylist(UUID id);

    SystemPlaylistResponse addSongToSystemPlaylist(UUID id, AddSongToSystemPlaylistRequest request);

    SystemPlaylistResponse removeSongFromSystemPlaylist(UUID id, UUID songId);

    SystemPlaylistResponse reorderSystemPlaylistSongs(UUID id, ReorderSystemPlaylistSongsRequest request);
}
