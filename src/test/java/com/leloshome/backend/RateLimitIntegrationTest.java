package com.leloshome.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.leloshome.backend.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reativa o rate limiting (desligado no perfil de teste) com capacidade pequena
 * para validar o bloqueio 429 no login. Usa @DynamicPropertySource (precedência
 * máxima) → contexto próprio com o filtro ligado.
 */
class RateLimitIntegrationTest extends AbstractIntegrationTest {

    @DynamicPropertySource
    static void rateLimitProps(DynamicPropertyRegistry registry) {
        registry.add("ratelimit.enabled", () -> "true");
        registry.add("ratelimit.login.capacity", () -> "3");
        registry.add("ratelimit.login.refill-seconds", () -> "60");
    }

    @Test
    void login_excedendoCapacidade_retorna429() {
        LoginRequest credenciais = new LoginRequest("admin@leloshome.com", "senha-errada");

        int naoLimitadas = 0;
        int limitadas = 0;
        for (int i = 0; i < 6; i++) {
            ResponseEntity<JsonNode> r = post("/api/admin/auth/login", credenciais, null);
            if (r.getStatusCode().value() == 429) {
                limitadas++;
            } else {
                naoLimitadas++; // 401 (credenciais inválidas) até esgotar o bucket
            }
        }

        // capacity=3 → no máximo 3 passam pelo filtro; o restante vira 429
        assertThat(naoLimitadas).isLessThanOrEqualTo(3);
        assertThat(limitadas).isGreaterThanOrEqualTo(1);
    }

    @Test
    void respostas429_trazemRetryAfter() {
        LoginRequest credenciais = new LoginRequest("admin@leloshome.com", "senha-errada");
        ResponseEntity<JsonNode> last = null;
        for (int i = 0; i < 8; i++) {
            last = post("/api/admin/auth/login", credenciais, null);
        }
        assertThat(last).isNotNull();
        assertThat(last.getStatusCode().value()).isEqualTo(429);
        assertThat(last.getHeaders().getFirst("Retry-After")).isNotBlank();
    }
}
