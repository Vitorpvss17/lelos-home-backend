package com.leloshome.backend.controller.admin;

import com.leloshome.backend.dto.request.ProductRequest;
import com.leloshome.backend.dto.response.ProductResponse;
import com.leloshome.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin · Produtos", description = "CRUD de produtos (requer Bearer JWT)")
@SecurityRequirement(name = "bearer-jwt")
public class ProductAdminController {

    private final ProductService service;

    @GetMapping
    @Operation(summary = "Lista todos os produtos (ativos e inativos)")
    public ResponseEntity<Page<ProductResponse>> listAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(service.listAll(pageable));
    }

    @PostMapping
    @Operation(summary = "Cria um produto")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um produto")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Alterna o status ativo/inativo do produto")
    public ResponseEntity<ProductResponse> toggle(@PathVariable UUID id) {
        return ResponseEntity.ok(service.toggle(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um produto")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
