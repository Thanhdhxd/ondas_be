package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.SearchHistory;

import java.util.List;
import java.util.UUID;

public interface SearchHistoryRepoPort {

    SearchHistory save(SearchHistory searchHistory);

    List<SearchHistory> findRecentByUserId(UUID userId, int limit);

    List<String> findTrendingQueries(int limit, int days);

    void deleteAllByUserId(UUID userId);

    void deleteByUserIdAndQuery(UUID userId, String query);
}
