package com.leloshome.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.leloshome.backend.domain.ProductType;
import com.leloshome.backend.dto.request.ProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductAdminIntegrationTest extends AbstractIntegrationTest {

    @Test
    void crudCompleto_deProduto() {
        String token = adminToken();
        UUID categoryId = UUID.fromString(createCategory(token, "Cat Produto " + UUID.randomUUID()));

        // CREATE
        ProductRequest create = new ProductRequest("Taça Cristal", "desc", ProductType.BOTH,
                new BigDecimal("50.00"), new BigDecimal("15.00"), List.of("http://img/t.png"), 100, categoryId);
        ResponseEntity<JsonNode> created = post("/api/admin/products", create, token);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String id = created.getBody().get("id").asText();

        // LIST paginada
        ResponseEntity<JsonNode> list = get("/api/admin/products", token);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody().get("totalElements").asInt()).isGreaterThanOrEqualTo(1);

        // UPDATE
        ProductRequest update = new ProductRequest("Taça Cristal v2", "upd", ProductType.RENT,
                null, new BigDecimal("18.00"), List.of(), 80, categoryId);
        ResponseEntity<JsonNode> updated = put("/api/admin/products/" + id, update, token);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().get("name").asText()).isEqualTo("Taça Cristal v2");

        // TOGGLE
        ResponseEntity<JsonNode> toggled = patch("/api/admin/products/" + id + "/toggle", null, token);
        assertThat(toggled.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(toggled.getBody().get("active").asBoolean()).isFalse();

        // DELETE
        assertThat(delete("/api/admin/products/" + id, token).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void create_semType_retorna400() {
        ResponseEntity<JsonNode> r = post("/api/admin/products",
                Map.of("name", "SemTipo", "salePrice", 10.0), adminToken());
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void create_comCategoriaInexistente_retorna404() {
        ProductRequest req = new ProductRequest("X", null, ProductType.SALE,
                new BigDecimal("10.00"), null, List.of(), 1,
                UUID.fromString("00000000-0000-0000-0000-000000000000"));
        ResponseEntity<JsonNode> r = post("/api/admin/products", req, adminToken());
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
