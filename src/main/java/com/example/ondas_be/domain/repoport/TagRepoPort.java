package com.example.ondas_be.domain.repoport;

import com.example.ondas_be.domain.entity.Tag;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepoPort {

    Tag save(Tag tag);

    Optional<Tag> findById(Long id);

    List<Tag> findAll();

    List<Tag> findByType(String type);

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByName(String name);

    List<Tag> findByIds(Collection<Long> ids);

    List<Tag> findByNameContains(String query, int page, int size);

    long countByNameContains(String query);

    List<Tag> findByNameFullText(String query, int page, int size);

    long countByNameFullText(String query);
}
