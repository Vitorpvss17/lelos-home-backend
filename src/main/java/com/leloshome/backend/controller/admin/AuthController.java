package com.leloshome.backend.controller.admin;

import com.leloshome.backend.dto.request.LoginRequest;
import com.leloshome.backend.dto.request.RefreshRequest;
import com.leloshome.backend.dto.response.LoginResponse;
import com.leloshome.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Admin · Auth", description = "Autenticação de operadores da Lelos Home")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autentica e retorna access + refresh token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renova o access token a partir de um refresh token válido")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}
