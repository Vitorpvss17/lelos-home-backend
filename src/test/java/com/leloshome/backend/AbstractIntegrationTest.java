package com.leloshome.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.leloshome.backend.domain.ProductType;
import com.leloshome.backend.dto.request.CategoryRequest;
import com.leloshome.backend.dto.request.LoginRequest;
import com.leloshome.backend.dto.request.ProductRequest;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Base para os testes de integração: sobe a aplicação completa em uma porta
 * aleatória contra o PostgreSQL local (perfil "test") e expõe helpers HTTP.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @Autowired
    protected TestRestTemplate rest;

    // Credenciais do admin vêm da config (resolvidas do .env), não hardcoded.
    @Value("${admin.email}")
    protected String adminEmail;

    @Value("${admin.password}")
    protected String adminPassword;

    @BeforeEach
    void enablePatchSupport() {
        // O cliente padrão (JDK) não suporta PATCH; troca para o Apache HttpClient 5.
        // disableAutomaticRetries: evita que o cliente re-tente respostas 429 (honrando
        // Retry-After), o que mascararia o rate limiting e tornaria os testes não-determinísticos.
        rest.getRestTemplate().setRequestFactory(
                new HttpComponentsClientHttpRequestFactory(
                        HttpClients.custom().disableAutomaticRetries().build()));
    }

    // ── Helpers HTTP ─────────────────────────────────────────────────────────

    protected String adminToken() {
        ResponseEntity<JsonNode> r = rest.postForEntity("/api/admin/auth/login",
                new LoginRequest(adminEmail, adminPassword), JsonNode.class);
        return r.getBody().get("accessToken").asText();
    }

    protected HttpHeaders headers(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            h.setBearerAuth(token);
        }
        return h;
    }

    protected ResponseEntity<JsonNode> get(String path, String token) {
        return rest.exchange(path, HttpMethod.GET, new HttpEntity<>(headers(token)), JsonNode.class);
    }

    protected ResponseEntity<JsonNode> post(String path, Object body, String token) {
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers(token)), JsonNode.class);
    }

    protected ResponseEntity<JsonNode> put(String path, Object body, String token) {
        return rest.exchange(path, HttpMethod.PUT, new HttpEntity<>(body, headers(token)), JsonNode.class);
    }

    protected ResponseEntity<JsonNode> patch(String path, Object body, String token) {
        return rest.exchange(path, HttpMethod.PATCH, new HttpEntity<>(body, headers(token)), JsonNode.class);
    }

    protected ResponseEntity<JsonNode> delete(String path, String token) {
        return rest.exchange(path, HttpMethod.DELETE, new HttpEntity<>(headers(token)), JsonNode.class);
    }

    // ── Fixtures reutilizáveis ───────────────────────────────────────────────

    protected String createCategory(String token, String name) {
        return post("/api/admin/categories", new CategoryRequest(name, "descrição", null), token)
                .getBody().get("id").asText();
    }

    protected String createProduct(String token, String categoryId, String name) {
        ProductRequest req = new ProductRequest(
                name, "descrição", ProductType.BOTH,
                new BigDecimal("50.00"), new BigDecimal("15.00"),
                List.of("http://img/x.png"), 100,
                categoryId != null ? UUID.fromString(categoryId) : null);
        return post("/api/admin/products", req, token).getBody().get("id").asText();
    }
}
