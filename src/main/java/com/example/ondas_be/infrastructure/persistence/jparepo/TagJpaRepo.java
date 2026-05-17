package com.example.ondas_be.infrastructure.persistence.jparepo;

import com.example.ondas_be.infrastructure.persistence.model.TagModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagJpaRepo extends JpaRepository<TagModel, Long> {

    boolean existsByName(String name);

    List<TagModel> findByType(String type);

    @Query(value = "select * from tags t where t.name ilike concat('%', :query, '%')",
        countQuery = "select count(*) from tags t where t.name ilike concat('%', :query, '%')",
        nativeQuery = true)
    Page<TagModel> findByNameContains(@Param("query") String query, Pageable pageable);

    @Query(value = "select count(*) from tags t where t.name ilike concat('%', :query, '%')", nativeQuery = true)
    long countByNameContains(@Param("query") String query);

    @Query(value = "select * from tags t where to_tsvector('simple', t.name) @@ plainto_tsquery('simple', :query)",
        countQuery = "select count(*) from tags t where to_tsvector('simple', t.name) @@ plainto_tsquery('simple', :query)",
        nativeQuery = true)
    Page<TagModel> findByNameFullText(@Param("query") String query, Pageable pageable);

    @Query(value = "select count(*) from tags t where to_tsvector('simple', t.name) @@ plainto_tsquery('simple', :query)",
        nativeQuery = true)
    long countByNameFullText(@Param("query") String query);
}
