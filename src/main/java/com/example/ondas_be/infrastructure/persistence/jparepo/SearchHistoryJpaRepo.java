package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.SearchHistoryModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface SearchHistoryJpaRepo extends JpaRepository<SearchHistoryModel, Long> {

    @Query("select s from SearchHistoryModel s where s.userId = :userId order by s.searchedAt desc")
    List<SearchHistoryModel> findRecentByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query(value = """
            select query
            from search_histories
            where searched_at >= :since
            group by query
            order by count(*) desc
            limit :limit
            """, nativeQuery = true)
    List<String> findTrendingQueries(@Param("since") LocalDateTime since, @Param("limit") int limit);

    @Modifying
    @Query("delete from SearchHistoryModel s where s.userId = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("delete from SearchHistoryModel s where s.userId = :userId and s.query = :query")
    void deleteByUserIdAndQuery(@Param("userId") UUID userId, @Param("query") String query);
}
