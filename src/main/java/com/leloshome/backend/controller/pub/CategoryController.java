package com.leloshome.backend.controller.pub;

import com.leloshome.backend.dto.response.CategoryResponse;
import com.leloshome.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Endpoints públicos de categorias")
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    @Operation(summary = "Lista categorias ativas")
    public ResponseEntity<List<CategoryResponse>> listActive() {
        return ResponseEntity.ok(service.listActive());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca categoria por ID")
    public ResponseEntity<CategoryResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
