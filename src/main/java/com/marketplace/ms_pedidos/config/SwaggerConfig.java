package com.marketplace.ms_pedidos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de Swagger / OpenAPI para ms-pedidos.
 * Swagger UI: http://localhost:8085/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS-Pedidos API")
                        .version("1.0")
                        .description("Gestion de pedidos del marketplace. " +
                                     "Se comunica con ms-productos y ms-usuarios via FeignClient."));
    }
}
