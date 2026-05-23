package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.AlbumModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AlbumJpaRepo extends JpaRepository<AlbumModel, UUID> {

    boolean existsBySlug(String slug);

    @Query(value = "select * from albums a where a.title ilike concat('%', :query, '%') order by lower(a.title), a.id",
        countQuery = "select count(*) from albums a where a.title ilike concat('%', :query, '%')",
        nativeQuery = true)
    Page<AlbumModel> findByTitleContains(@Param("query") String query, Pageable pageable);

    @Query(value = "select count(*) from albums a where a.title ilike concat('%', :query, '%')", nativeQuery = true)
    long countByTitleContains(@Param("query") String query);

    @Query(value = "select * from albums a where to_tsvector('simple', a.title) @@ plainto_tsquery('simple', :query)",
        countQuery = "select count(*) from albums a where to_tsvector('simple', a.title) @@ plainto_tsquery('simple', :query)",
        nativeQuery = true)
    Page<AlbumModel> findByTitleFullText(@Param("query") String query, Pageable pageable);

    @Query(value = "select count(*) from albums a where to_tsvector('simple', a.title) @@ plainto_tsquery('simple', :query)",
        nativeQuery = true)
    long countByTitleFullText(@Param("query") String query);
}
