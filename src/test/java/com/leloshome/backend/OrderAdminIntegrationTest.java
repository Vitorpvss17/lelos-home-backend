package com.leloshome.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.leloshome.backend.domain.ItemMode;
import com.leloshome.backend.domain.OrderItemType;
import com.leloshome.backend.dto.request.OrderItemRequest;
import com.leloshome.backend.dto.request.OrderRequest;
import com.leloshome.backend.dto.request.OrderStatusUpdateRequest;
import com.leloshome.backend.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderAdminIntegrationTest extends AbstractIntegrationTest {

    private UUID novoProduto() {
        String token = adminToken();
        String catId = createCategory(token, "Cat Pedido " + UUID.randomUUID());
        return UUID.fromString(createProduct(token, catId, "Prod Pedido " + UUID.randomUUID()));
    }

    private String criarPedido(UUID productId) {
        OrderRequest req = new OrderRequest("Maria E2E", "11999999999",
                LocalDate.of(2026, 12, 15), "obs",
                List.of(new OrderItemRequest(OrderItemType.PRODUCT, productId, null, 10, ItemMode.RENT)));
        ResponseEntity<JsonNode> r = post("/api/orders", req, null);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return r.getBody().get("id").asText();
    }

    @Test
    void criacaoPublica_eGestaoAdmin() {
        String token = adminToken();
        String orderId = criarPedido(novoProduto());

        // lista
        assertThat(get("/api/admin/orders", token).getBody().get("totalElements").asInt())
                .isGreaterThanOrEqualTo(1);

        // filtro por status (pedido nasce SENT_TO_WHATSAPP)
        assertThat(get("/api/admin/orders?status=SENT_TO_WHATSAPP", token)
                .getBody().get("totalElements").asInt()).isGreaterThanOrEqualTo(1);

        // detalhe por id
        assertThat(get("/api/admin/orders/" + orderId, token).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        // atualiza status
        ResponseEntity<JsonNode> patched = patch("/api/admin/orders/" + orderId + "/status",
                new OrderStatusUpdateRequest(OrderStatus.CANCELLED), token);
        assertThat(patched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patched.getBody().get("status").asText()).isEqualTo("CANCELLED");
    }

    @Test
    void filtroPorData_funciona() {
        String token = adminToken();
        criarPedido(novoProduto());

        String hoje = LocalDate.now().toString();
        String ontem = LocalDate.now().minusDays(1).toString();
        String amanha = LocalDate.now().plusDays(1).toString();

        assertThat(get("/api/admin/orders?createdFrom=" + hoje + "&createdTo=" + hoje, token)
                .getBody().get("totalElements").asInt()).isGreaterThanOrEqualTo(1);

        assertThat(get("/api/admin/orders?createdTo=" + ontem, token)
                .getBody().get("totalElements").asInt()).isZero();

        assertThat(get("/api/admin/orders?createdFrom=" + amanha, token)
                .getBody().get("totalElements").asInt()).isZero();

        // combinação status + data
        assertThat(get("/api/admin/orders?status=SENT_TO_WHATSAPP&createdFrom=" + hoje + "&createdTo=" + hoje, token)
                .getBody().get("totalElements").asInt()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void filtro_comDataInvalida_retorna400() {
        assertThat(get("/api/admin/orders?createdFrom=not-a-date", adminToken()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void filtro_comStatusInvalido_retorna400() {
        assertThat(get("/api/admin/orders?status=FOO", adminToken()).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void atualizarStatus_dePedidoInexistente_retorna404() {
        String fakeId = "00000000-0000-0000-0000-000000000000";
        ResponseEntity<JsonNode> r = patch("/api/admin/orders/" + fakeId + "/status",
                new OrderStatusUpdateRequest(OrderStatus.CANCELLED), adminToken());
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void detalheDePedido_naoEhMaisPublico() {
        String orderId = criarPedido(novoProduto());
        // público (sem token) → bloqueado: não expõe mais PII por UUID
        assertThat(get("/api/orders/" + orderId, null).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        // admin (autenticado) → detalhe disponível
        assertThat(get("/api/admin/orders/" + orderId, adminToken()).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }
}
