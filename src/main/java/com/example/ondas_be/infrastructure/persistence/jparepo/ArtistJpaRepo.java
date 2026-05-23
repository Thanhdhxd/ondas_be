package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.ArtistModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ArtistJpaRepo extends JpaRepository<ArtistModel, UUID> {

    boolean existsBySlug(String slug);

    @Query(value = "select * from artists a where a.name ilike concat('%', :query, '%') order by lower(a.name), a.id",
        countQuery = "select count(*) from artists a where a.name ilike concat('%', :query, '%')",
        nativeQuery = true)
    Page<ArtistModel> findByNameContains(@Param("query") String query, Pageable pageable);

    @Query(value = "select count(*) from artists a where a.name ilike concat('%', :query, '%')", nativeQuery = true)
    long countByNameContains(@Param("query") String query);

    @Query(value = "select * from artists a where to_tsvector('simple', a.name) @@ plainto_tsquery('simple', :query)",
        countQuery = "select count(*) from artists a where to_tsvector('simple', a.name) @@ plainto_tsquery('simple', :query)",
        nativeQuery = true)
    Page<ArtistModel> findByNameFullText(@Param("query") String query, Pageable pageable);

    @Query(value = "select count(*) from artists a where to_tsvector('simple', a.name) @@ plainto_tsquery('simple', :query)",
        nativeQuery = true)
    long countByNameFullText(@Param("query") String query);
}
