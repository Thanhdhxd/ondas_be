package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.response.AdminDailyPlayResponse;
import com.example.ondas_be.application.dto.response.AdminDauMauResponse;
import com.example.ondas_be.application.dto.response.AdminTopArtistResponse;
import com.example.ondas_be.application.dto.response.AdminTopSongResponse;

import java.time.LocalDate;
import java.util.List;

public interface AdminStatsServicePort {

    List<AdminTopSongResponse> getTopSongs(LocalDate from, LocalDate to, Integer limit);

    List<AdminTopArtistResponse> getTopArtists(LocalDate from, LocalDate to, Integer limit);

    List<AdminDailyPlayResponse> getDailyPlays(LocalDate from, LocalDate to);

    AdminDauMauResponse getDauMau(LocalDate date);
}
