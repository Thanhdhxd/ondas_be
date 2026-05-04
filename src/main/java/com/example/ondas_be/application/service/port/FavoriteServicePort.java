package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.response.FavoriteSongResponse;

import java.util.UUID;

public interface FavoriteServicePort {

    /**
     * Adds a song to the user's favorites list.
     *
     * @param email  authenticated user's email
     * @param songId song to add
     */
    void addFavorite(String email, UUID songId);

    /**
     * Removes a song from the user's favorites list.
     *
     * @param email  authenticated user's email
     * @param songId song to remove
     */
    void removeFavorite(String email, UUID songId);

    /**
     * Checks whether a specific song is in the user's favorites.
     *
     * @param email  authenticated user's email
     * @param songId song to check
     * @return true if the song is a favorite
     */
    boolean isFavorite(String email, UUID songId);

    /**
     * Returns a paginated list of the user's favorite songs.
     *
     * @param email authenticated user's email
     * @param page  zero-based page index
     * @param size  page size
     * @return paginated favorite songs
     */
    PageResultDto<FavoriteSongResponse> getFavorites(String email, int page, int size);
}
