package com.leloshome.backend.controller.admin;

import com.leloshome.backend.dto.request.CategoryRequest;
import com.leloshome.backend.dto.response.CategoryResponse;
import com.leloshome.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin · Categorias", description = "CRUD de categorias (requer Bearer JWT)")
@SecurityRequirement(name = "bearer-jwt")
public class CategoryAdminController {

    private final CategoryService service;

    @GetMapping
    @Operation(summary = "Lista todas as categorias (ativas e inativas)")
    public ResponseEntity<List<CategoryResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }

    @PostMapping
    @Operation(summary = "Cria uma categoria")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma categoria")
    public ResponseEntity<CategoryResponse> update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Alterna o status ativo/inativo da categoria")
    public ResponseEntity<CategoryResponse> toggle(@PathVariable UUID id) {
        return ResponseEntity.ok(service.toggle(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma categoria")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
