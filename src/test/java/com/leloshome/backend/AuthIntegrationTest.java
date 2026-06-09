package com.leloshome.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.leloshome.backend.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void login_comCredenciaisCorretas_retornaTokens() {
        ResponseEntity<JsonNode> r = post("/api/admin/auth/login",
                new LoginRequest(adminEmail, adminPassword), null);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("accessToken").asText()).isNotBlank();
        assertThat(r.getBody().get("refreshToken").asText()).isNotBlank();
        assertThat(r.getBody().get("expiresIn").asLong()).isPositive();
    }

    @Test
    void login_comSenhaErrada_retorna401() {
        ResponseEntity<JsonNode> r = post("/api/admin/auth/login",
                new LoginRequest(adminEmail, "errada"), null);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_comEmailInvalido_retorna400() {
        ResponseEntity<JsonNode> r = post("/api/admin/auth/login",
                Map.of("email", "nao-eh-email", "password", "x"), null);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_comUsuarioInexistente_retorna401() {
        ResponseEntity<JsonNode> r = post("/api/admin/auth/login",
                new LoginRequest("naoexiste@x.com", adminPassword), null);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rotaAdmin_semToken_retorna401() {
        assertThat(get("/api/admin/categories", null).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rotaAdmin_comAccessToken_autentica() {
        assertThat(get("/api/admin/categories", adminToken()).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void refresh_comRefreshTokenValido_retornaNovosTokens() {
        String refresh = post("/api/admin/auth/login",
                new LoginRequest(adminEmail, adminPassword), null)
                .getBody().get("refreshToken").asText();

        ResponseEntity<JsonNode> r = post("/api/admin/auth/refresh",
                Map.of("refreshToken", refresh), null);

        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getBody().get("accessToken").asText()).isNotBlank();
    }

    @Test
    void refresh_usandoAccessToken_retorna401() {
        String access = adminToken();
        ResponseEntity<JsonNode> r = post("/api/admin/auth/refresh",
                Map.of("refreshToken", access), null);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rotaProtegida_usandoRefreshTokenComoBearer_retorna401() {
        String refresh = post("/api/admin/auth/login",
                new LoginRequest(adminEmail, adminPassword), null)
                .getBody().get("refreshToken").asText();

        assertThat(get("/api/admin/categories", refresh).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void rotaProtegida_comTokenLixo_retorna401() {
        assertThat(get("/api/admin/categories", "abc.def.ghi").getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logout_revogaTokensNoServidor() {
        JsonNode tokens = post("/api/admin/auth/login",
                new LoginRequest(adminEmail, adminPassword), null).getBody();
        String access = tokens.get("accessToken").asText();
        String refresh = tokens.get("refreshToken").asText();

        // token funciona antes do logout
        assertThat(get("/api/admin/categories", access).getStatusCode()).isEqualTo(HttpStatus.OK);

        // logout (autenticado) → 204
        assertThat(post("/api/admin/auth/logout", null, access).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);

        // o MESMO access token agora é rejeitado (revogado no servidor)
        assertThat(get("/api/admin/categories", access).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        // e o refresh token também não renova mais
        assertThat(post("/api/admin/auth/refresh", Map.of("refreshToken", refresh), null).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logout_semToken_retorna401() {
        assertThat(post("/api/admin/auth/logout", null, null).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
