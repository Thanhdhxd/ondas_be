package com.example.ondas_be.infrastructure.persistence.adapter;

import com.example.ondas_be.domain.entity.Tag;
import com.example.ondas_be.domain.repoport.TagRepoPort;
import com.example.ondas_be.infrastructure.persistence.jparepo.TagJpaRepo;
import com.example.ondas_be.infrastructure.persistence.model.TagModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TagAdapter implements TagRepoPort {

    private final TagJpaRepo tagJpaRepo;

    @Override
    public Tag save(Tag tag) {
        return tagJpaRepo.save(TagModel.fromDomain(tag)).toDomain();
    }

    @Override
    public Optional<Tag> findById(Long id) {
        return tagJpaRepo.findById(id).map(TagModel::toDomain);
    }

    @Override
    public List<Tag> findAll() {
        return tagJpaRepo.findAll().stream().map(TagModel::toDomain).toList();
    }

    @Override
    public List<Tag> findByType(String type) {
        return tagJpaRepo.findByType(type).stream().map(TagModel::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        tagJpaRepo.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return tagJpaRepo.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return tagJpaRepo.existsByName(name);
    }

    @Override
    public List<Tag> findByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return tagJpaRepo.findAllById(ids).stream()
                .map(TagModel::toDomain)
                .toList();
    }

    @Override
    public List<Tag> findByNameContains(String query, int page, int size) {
        return tagJpaRepo.findByNameContains(query, PageRequest.of(page, size))
                .map(TagModel::toDomain)
                .toList();
    }

    @Override
    public long countByNameContains(String query) {
        return tagJpaRepo.countByNameContains(query);
    }

    @Override
    public List<Tag> findByNameFullText(String query, int page, int size) {
        return tagJpaRepo.findByNameFullText(query, PageRequest.of(page, size))
                .map(TagModel::toDomain)
                .toList();
    }

    @Override
    public long countByNameFullText(String query) {
        return tagJpaRepo.countByNameFullText(query);
    }
}
