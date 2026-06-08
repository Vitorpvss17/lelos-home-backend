package com.leloshome.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /** Nome do esquema de segurança referenciado via @SecurityRequirement nos controllers admin. */
    public static final String BEARER_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI lelosHomeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Lelos Home API")
                        .description("""
                                API REST da Lelos Home — catálogo de produtos e kits para mesa posta,
                                criação de pedidos com redirecionamento para o WhatsApp e administração
                                protegida por JWT.

                                **Como autenticar nas rotas /api/admin/**:**
                                1. Faça `POST /api/admin/auth/login` com e-mail e senha.
                                2. Copie o `accessToken` da resposta.
                                3. Clique em **Authorize** (cadeado) e cole o token.
                                """)
                        .version("v1")
                        .contact(new Contact().name("Lelos Home").email("contato@leloshome.com")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Informe apenas o accessToken (sem o prefixo \"Bearer \").")));
    }
}
