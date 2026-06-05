package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.PlayHistoryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayHistoryJpaRepo extends JpaRepository<PlayHistoryModel, Long> {

    Page<PlayHistoryModel> findByUserIdOrderByPlayedAtDesc(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

    Optional<PlayHistoryModel> findByIdAndUserId(Long id, UUID userId);

    @Modifying
    @Query("delete from PlayHistoryModel p where p.userId = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("delete from PlayHistoryModel p where p.id = :id and p.userId = :userId")
    void deleteByIdAndUserId(@Param("id") Long id, @Param("userId") UUID userId);

    @Query(value = """
        select ph.song_id as songId, count(*) as playCount
        from play_histories ph
        where ph.played_at >= :from and ph.played_at < :to
        group by ph.song_id
        order by playCount desc
        limit :limit
    """, nativeQuery = true)
    List<SongPlayCountProjection> findTopSongs(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("limit") int limit);

    @Query(value = """
        select sa.artist_id as artistId, count(*) as playCount
        from play_histories ph
        join song_artists sa on sa.song_id = ph.song_id
        where ph.played_at >= :from and ph.played_at < :to
        group by sa.artist_id
        order by playCount desc
        limit :limit
    """, nativeQuery = true)
    List<ArtistPlayCountProjection> findTopArtists(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("limit") int limit);

    @Query(value = """
        select cast(ph.played_at as date) as day, count(*) as playCount
        from play_histories ph
        where ph.played_at >= :from and ph.played_at < :to
        group by day
        order by day
    """, nativeQuery = true)
    List<DailyPlayCountProjection> findDailyPlays(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = """
        select count(distinct ph.user_id)
        from play_histories ph
        where ph.played_at >= :from and ph.played_at < :to
    """, nativeQuery = true)
    long countDistinctUsers(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = """
        select coalesce(sum(coalesce(ph.duration_played_seconds, s.duration_seconds)), 0)
        from play_histories ph
        join songs s on s.id = ph.song_id
        where ph.user_id = :userId
    """, nativeQuery = true)
    long sumListeningDurationByUserId(@Param("userId") UUID userId);

    @Query(value = """
        select ph.song_id as songId, count(*) as playCount
        from play_histories ph
        where ph.user_id = :userId
        group by ph.song_id
        order by playCount desc
        limit :limit
    """, nativeQuery = true)
    List<SongPlayCountProjection> findMyTopSongs(
            @Param("userId") UUID userId,
            @Param("limit") int limit);

    @Query(value = """
        select sa.artist_id as artistId, count(*) as playCount
        from play_histories ph
        join song_artists sa on sa.song_id = ph.song_id
        where ph.user_id = :userId
        group by sa.artist_id
        order by playCount desc
        limit :limit
    """, nativeQuery = true)
    List<ArtistPlayCountProjection> findMyTopArtists(
            @Param("userId") UUID userId,
            @Param("limit") int limit);
}
