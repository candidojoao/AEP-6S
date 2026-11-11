package br.com.ecofood.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenApiConfigTest {

    @Test
    void deveDescreverAApiNoSwagger() {
        OpenAPI openApi = new OpenApiConfig().ecoFoodOpenApi();

        assertEquals("EcoFood API", openApi.getInfo().getTitle());
        assertEquals("2.0", openApi.getInfo().getVersion());
    }
}
