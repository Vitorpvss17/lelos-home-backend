package com.leloshome.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.leloshome.backend.domain.ProductType;
import com.leloshome.backend.dto.request.KitItemRequest;
import com.leloshome.backend.dto.request.KitRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KitAdminIntegrationTest extends AbstractIntegrationTest {

    @Test
    void crudCompleto_deKitComItens() {
        String token = adminToken();
        String catId = createCategory(token, "Cat Kit " + UUID.randomUUID());
        UUID productId = UUID.fromString(createProduct(token, catId, "Prod Kit " + UUID.randomUUID()));

        // CREATE com 1 item
        KitRequest create = new KitRequest("Kit Mesa", "desc", ProductType.RENT,
                null, new BigDecimal("200.00"), List.of("http://img/k.png"),
                List.of(new KitItemRequest(productId, 4)));
        ResponseEntity<JsonNode> created = post("/api/admin/kits", create, token);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String kitId = created.getBody().get("id").asText();
        assertThat(created.getBody().get("items")).hasSize(1);
        assertThat(created.getBody().get("items").get(0).get("quantity").asInt()).isEqualTo(4);

        // UPDATE troca a quantidade do item
        KitRequest update = new KitRequest("Kit Mesa v2", "upd", ProductType.RENT,
                null, new BigDecimal("250.00"), List.of(),
                List.of(new KitItemRequest(productId, 6)));
        ResponseEntity<JsonNode> updated = put("/api/admin/kits/" + kitId, update, token);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().get("items")).hasSize(1);
        assertThat(updated.getBody().get("items").get(0).get("quantity").asInt()).isEqualTo(6);

        // TOGGLE
        assertThat(patch("/api/admin/kits/" + kitId + "/toggle", null, token)
                .getBody().get("active").asBoolean()).isFalse();

        // LIST
        assertThat(get("/api/admin/kits", token).getStatusCode()).isEqualTo(HttpStatus.OK);

        // DELETE
        assertThat(delete("/api/admin/kits/" + kitId, token).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void create_comProdutoInexistente_retorna404() {
        String token = adminToken();
        KitRequest req = new KitRequest("Kit Ruim", null, ProductType.RENT,
                null, new BigDecimal("10.00"), List.of(),
                List.of(new KitItemRequest(
                        UUID.fromString("00000000-0000-0000-0000-000000000000"), 1)));
        assertThat(post("/api/admin/kits", req, token).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
