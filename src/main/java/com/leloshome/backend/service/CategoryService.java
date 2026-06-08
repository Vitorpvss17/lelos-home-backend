package com.leloshome.backend.service;

import com.leloshome.backend.domain.Category;
import com.leloshome.backend.dto.request.CategoryRequest;
import com.leloshome.backend.dto.response.CategoryResponse;
import com.leloshome.backend.exception.ResourceNotFoundException;
import com.leloshome.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    public List<CategoryResponse> listActive() {
        return repository.findAllByActiveTrue().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<CategoryResponse> listAll() {
        return repository.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse findById(UUID id) {
        return CategoryResponse.from(getOrThrow(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        Category cat = Category.builder()
                .name(req.name())
                .description(req.description())
                .imageUrl(req.imageUrl())
                .build();
        return CategoryResponse.from(repository.save(cat));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest req) {
        Category cat = getOrThrow(id);
        cat.setName(req.name());
        cat.setDescription(req.description());
        cat.setImageUrl(req.imageUrl());
        return CategoryResponse.from(repository.save(cat));
    }

    @Transactional
    public CategoryResponse toggle(UUID id) {
        Category cat = getOrThrow(id);
        cat.setActive(!cat.getActive());
        return CategoryResponse.from(repository.save(cat));
    }

    @Transactional
    public void delete(UUID id) {
        getOrThrow(id);
        repository.deleteById(id);
    }

    private Category getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada: " + id));
    }
}
