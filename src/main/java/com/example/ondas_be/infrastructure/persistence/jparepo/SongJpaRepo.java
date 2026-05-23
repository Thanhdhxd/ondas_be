package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.SongModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SongJpaRepo extends JpaRepository<SongModel, UUID> {

    boolean existsById(UUID id);

    boolean existsBySlug(String slug);

    List<SongModel> findByAlbumIdOrderByTrackNumber(UUID albumId);

    Page<SongModel> findByAlbumId(UUID albumId, Pageable pageable);

    long countByAlbumId(UUID albumId);

    @Query("select s from SongModel s join SongArtistModel sa on sa.id.songId = s.id where sa.id.artistId = :artistId")
    Page<SongModel> findByArtistId(@Param("artistId") UUID artistId, Pageable pageable);

    @Query("select count(s) from SongModel s join SongArtistModel sa on sa.id.songId = s.id where sa.id.artistId = :artistId")
    long countByArtistId(@Param("artistId") UUID artistId);

    @Query("select s from SongModel s join SongGenreModel sg on sg.id.songId = s.id where sg.id.genreId = :genreId")
    Page<SongModel> findByGenreId(@Param("genreId") Long genreId, Pageable pageable);

    @Query("select count(s) from SongModel s join SongGenreModel sg on sg.id.songId = s.id where sg.id.genreId = :genreId")
    long countByGenreId(@Param("genreId") Long genreId);

        @Query(
            value = "select s.* from songs s join song_tags st on st.song_id = s.id " +
                "where st.tag_id in (:tagIds) " +
                "group by s.id " +
                "having count(distinct st.tag_id) = :tagCount",
            countQuery = "select count(*) from (" +
                "select s.id from songs s join song_tags st on st.song_id = s.id " +
                "where st.tag_id in (:tagIds) " +
                "group by s.id " +
                "having count(distinct st.tag_id) = :tagCount" +
                ") x",
            nativeQuery = true
        )
        Page<SongModel> findByTagIds(
            @Param("tagIds") List<Long> tagIds,
            @Param("tagCount") long tagCount,
            Pageable pageable
        );

        @Query(
            value = "select count(*) from (" +
                "select s.id from songs s join song_tags st on st.song_id = s.id " +
                "where st.tag_id in (:tagIds) " +
                "group by s.id " +
                "having count(distinct st.tag_id) = :tagCount" +
                ") x",
            nativeQuery = true
        )
        long countByTagIds(
            @Param("tagIds") List<Long> tagIds,
            @Param("tagCount") long tagCount
        );

        @Query(value = "select * from songs s where s.title ilike concat('%', :query, '%')",
            countQuery = "select count(*) from songs s where s.title ilike concat('%', :query, '%')",
            nativeQuery = true)
        Page<SongModel> findByTitleContains(@Param("query") String query, Pageable pageable);

            @Query(value = "select count(*) from songs s where s.title ilike concat('%', :query, '%')", nativeQuery = true)
            long countByTitleContains(@Param("query") String query);

        @Query(value = "select * from songs s where to_tsvector('simple', s.title) @@ plainto_tsquery('simple', :query)",
            countQuery = "select count(*) from songs s where to_tsvector('simple', s.title) @@ plainto_tsquery('simple', :query)",
            nativeQuery = true)
        Page<SongModel> findByTitleFullText(@Param("query") String query, Pageable pageable);

            @Query(value = "select count(*) from songs s where to_tsvector('simple', s.title) @@ plainto_tsquery('simple', :query)",
                nativeQuery = true)
            long countByTitleFullText(@Param("query") String query);

    @Modifying
    @Query("UPDATE SongModel s SET s.playCount = s.playCount + 1 WHERE s.id = :id")
    void incrementPlayCount(@Param("id") UUID id);

    List<SongModel> findByIdIn(List<UUID> ids);
}