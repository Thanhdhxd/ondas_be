package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.response.UserListeningTimeResponse;
import com.example.ondas_be.application.dto.response.UserTopArtistResponse;
import com.example.ondas_be.application.dto.response.UserTopSongResponse;

import java.util.List;

public interface UserStatsServicePort {

    UserListeningTimeResponse getMyListeningTime(String email);

    List<UserTopSongResponse> getMyTopSongs(String email, int limit);

    List<UserTopArtistResponse> getMyTopArtists(String email, int limit);
}
