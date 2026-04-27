package com.example.ondas_be.application.service.port;

import com.example.ondas_be.application.dto.common.PageResultDto;
import com.example.ondas_be.application.dto.response.PlayHistoryResponse;

import java.util.UUID;

public interface PlayHistoryServicePort {

    /**
     * Returns the paginated play history for the authenticated user, ordered by most recent first.
     *
     * @param email the user's email from the security context
     * @param page  0-based page index
     * @param size  page size
     * @return paginated play history
     */
    PageResultDto<PlayHistoryResponse> getMyHistory(String email, int page, int size);

    /**
     * Deletes all play history entries for the authenticated user.
     *
     * @param email the user's email from the security context
     */
    void clearMyHistory(String email);

    /**
     * Deletes a specific play history entry belonging to the authenticated user.
     *
     * @param email the user's email from the security context
     * @param id    the play history entry ID
     * @throws com.example.ondas_be.application.exception.PlayHistoryNotFoundException if the entry does not exist or does not belong to the user
     */
    void deleteHistoryEntry(String email, Long id);

    /**
     * Records a play event for the authenticated user: saves a play history entry and
     * increments the song's play count. Should be called once when playback actually starts.
     *
     * @param songId song identifier
     * @param email  authenticated user's email
     * @param source play source (e.g. "playlist", "home") — may be null
     */
    void recordPlay(UUID songId, String email, String source);
}
