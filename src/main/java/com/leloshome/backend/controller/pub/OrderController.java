package com.leloshome.backend.controller.pub;

import com.leloshome.backend.dto.request.OrderRequest;
import com.leloshome.backend.dto.response.OrderResponse;
import com.leloshome.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Endpoints públicos de pedidos")
public class OrderController {

    private final OrderService service;

    @PostMapping
    @Operation(summary = "Cria pedido e retorna link do WhatsApp")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta pedido por ID")
    public ResponseEntity<OrderResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
