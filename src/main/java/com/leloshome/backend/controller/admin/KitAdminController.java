package com.leloshome.backend.controller.admin;

import com.leloshome.backend.dto.request.KitRequest;
import com.leloshome.backend.dto.response.KitResponse;
import com.leloshome.backend.service.KitService;
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
@RequestMapping("/api/admin/kits")
@RequiredArgsConstructor
@Tag(name = "Admin · Kits", description = "CRUD de kits (requer Bearer JWT)")
@SecurityRequirement(name = "bearer-jwt")
public class KitAdminController {

    private final KitService service;

    @GetMapping
    @Operation(summary = "Lista todos os kits (ativos e inativos)")
    public ResponseEntity<Page<KitResponse>> listAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(service.listAll(pageable));
    }

    @PostMapping
    @Operation(summary = "Cria um kit com seus itens")
    public ResponseEntity<KitResponse> create(@Valid @RequestBody KitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um kit e seus itens")
    public ResponseEntity<KitResponse> update(@PathVariable UUID id, @Valid @RequestBody KitRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Alterna o status ativo/inativo do kit")
    public ResponseEntity<KitResponse> toggle(@PathVariable UUID id) {
        return ResponseEntity.ok(service.toggle(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um kit")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
