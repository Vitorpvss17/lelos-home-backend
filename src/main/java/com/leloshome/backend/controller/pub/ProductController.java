package com.leloshome.backend.controller.pub;

import com.leloshome.backend.domain.ProductType;
import com.leloshome.backend.dto.response.ProductResponse;
import com.leloshome.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Endpoints públicos de produtos")
public class ProductController {

    private final ProductService service;

    @GetMapping
    @Operation(summary = "Lista produtos ativos com filtros opcionais")
    public ResponseEntity<Page<ProductResponse>> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ProductType type,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(service.listActive(categoryId, type, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca produto por ID")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
