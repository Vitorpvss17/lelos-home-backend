package com.leloshome.backend.controller.admin;

import com.leloshome.backend.domain.OrderStatus;
import com.leloshome.backend.dto.request.OrderStatusUpdateRequest;
import com.leloshome.backend.dto.response.OrderResponse;
import com.leloshome.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Admin · Pedidos", description = "Visualização e gestão de pedidos (requer Bearer JWT)")
@SecurityRequirement(name = "bearer-jwt")
public class OrderAdminController {

    private final OrderService service;

    @GetMapping
    @Operation(summary = "Lista pedidos com filtros opcionais por status e intervalo de data de criação")
    public ResponseEntity<Page<OrderResponse>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listAll(status, createdFrom, createdTo, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um pedido por ID")
    public ResponseEntity<OrderResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza o status de um pedido")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(service.updateStatus(id, request.status()));
    }
}
