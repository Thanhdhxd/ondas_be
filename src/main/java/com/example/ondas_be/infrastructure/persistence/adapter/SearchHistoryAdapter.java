package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.SearchHistory;
import com.example.ondas_be.domain.repoport.SearchHistoryRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.SearchHistoryJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.SearchHistoryModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchHistoryAdapter implements SearchHistoryRepoPort {

    private final SearchHistoryJpaRepo searchHistoryJpaRepo;

    @Override
    @Transactional
    public SearchHistory save(SearchHistory searchHistory) {
        return searchHistoryJpaRepo.save(SearchHistoryModel.fromDomain(searchHistory)).toDomain();
    }

    @Override
    public List<SearchHistory> findRecentByUserId(UUID userId, int limit) {
        return searchHistoryJpaRepo.findRecentByUserId(userId, PageRequest.of(0, limit))
                .stream()
                .map(SearchHistoryModel::toDomain)
                .toList();
    }

    @Override
    public List<String> findTrendingQueries(int limit, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return searchHistoryJpaRepo.findTrendingQueries(since, limit);
    }

    @Override
    @Transactional
    public void deleteAllByUserId(UUID userId) {
        searchHistoryJpaRepo.deleteAllByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndQuery(UUID userId, String query) {
        searchHistoryJpaRepo.deleteByUserIdAndQuery(userId, query);
    }
}
