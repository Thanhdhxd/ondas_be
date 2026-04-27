package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.request.AddSongToPlaylistRequest;
import com.example.ondas_be.application.dto.request.CreatePlaylistRequest;
import com.example.ondas_be.application.dto.request.PlaylistFilterRequest;
import com.example.ondas_be.application.dto.request.ReorderPlaylistSongsRequest;
import com.example.ondas_be.application.dto.request.UpdatePlaylistRequest;
import com.example.ondas_be.application.dto.response.PlaylistResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface PlaylistServicePort {

    PlaylistResponse createPlaylist(String email, CreatePlaylistRequest request, MultipartFile coverFile);

    PlaylistResponse updatePlaylist(String email, UUID id, UpdatePlaylistRequest request, MultipartFile coverFile);

    PlaylistResponse getPlaylistById(String email, UUID id);

    PageResultDto<PlaylistResponse> getPlaylists(String email, PlaylistFilterRequest filter);

    void deletePlaylist(String email, UUID id);

    PlaylistResponse addSongToPlaylist(String email, UUID id, AddSongToPlaylistRequest request);

    PlaylistResponse removeSongFromPlaylist(String email, UUID id, UUID songId);

    PlaylistResponse reorderPlaylistSongs(String email, UUID id, ReorderPlaylistSongsRequest request);
}
