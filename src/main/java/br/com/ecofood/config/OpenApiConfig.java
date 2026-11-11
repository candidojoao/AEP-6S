package br.com.ecofood.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ecoFoodOpenApi() {
        return new OpenAPI().info(new Info()
                .title("EcoFood API")
                .version("2.0")
                .description("Controle dos alimentos armazenados nas residências para reduzir "
                        + "o desperdício (ODS 12 — Consumo e Produção Responsáveis)."));
    }
}
