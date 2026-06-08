package com.leloshome.backend.controller.pub;

import com.leloshome.backend.domain.ProductType;
import com.leloshome.backend.dto.response.KitResponse;
import com.leloshome.backend.service.KitService;
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
@RequestMapping("/api/kits")
@RequiredArgsConstructor
@Tag(name = "Kits", description = "Endpoints públicos de kits")
public class KitController {

    private final KitService service;

    @GetMapping
    @Operation(summary = "Lista kits ativos com filtro opcional por tipo")
    public ResponseEntity<Page<KitResponse>> list(
            @RequestParam(required = false) ProductType type,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(service.listActive(type, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca kit por ID com itens")
    public ResponseEntity<KitResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
