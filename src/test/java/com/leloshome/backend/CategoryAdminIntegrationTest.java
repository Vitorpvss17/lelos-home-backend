package com.leloshome.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.leloshome.backend.dto.request.CategoryRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryAdminIntegrationTest extends AbstractIntegrationTest {

    @Test
    void crudCompleto_deCategoria() {
        String token = adminToken();
        String nome = "Mesa Posta " + UUID.randomUUID();

        // CREATE
        ResponseEntity<JsonNode> created = post("/api/admin/categories",
                new CategoryRequest(nome, "cat", "http://img/c.png"), token);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String id = created.getBody().get("id").asText();
        assertThat(created.getBody().get("active").asBoolean()).isTrue();

        // LIST (todas)
        ResponseEntity<JsonNode> list = get("/api/admin/categories", token);
        assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(list.getBody().isArray()).isTrue();

        // UPDATE
        ResponseEntity<JsonNode> updated = put("/api/admin/categories/" + id,
                new CategoryRequest(nome + " v2", "upd", null), token);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().get("name").asText()).isEqualTo(nome + " v2");

        // TOGGLE -> inativo
        ResponseEntity<JsonNode> toggled = patch("/api/admin/categories/" + id + "/toggle", null, token);
        assertThat(toggled.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(toggled.getBody().get("active").asBoolean()).isFalse();

        // DELETE
        assertThat(delete("/api/admin/categories/" + id, token).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void create_comNomeVazio_retorna400() {
        ResponseEntity<JsonNode> r = post("/api/admin/categories", Map.of("name", ""), adminToken());
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void update_deCategoriaInexistente_retorna404() {
        String fakeId = "00000000-0000-0000-0000-000000000000";
        ResponseEntity<JsonNode> r = put("/api/admin/categories/" + fakeId,
                new CategoryRequest("X", null, null), adminToken());
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void create_semToken_retorna401() {
        ResponseEntity<JsonNode> r = post("/api/admin/categories",
                new CategoryRequest("X", null, null), null);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
