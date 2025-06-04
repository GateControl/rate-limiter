package org.example.ratelimiter.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tells OpenAPI that every operation *may* require an API key
 * sent as an HTTP header named X-API-Key.
 */
@Configuration
@SecurityScheme(
        name = "ApiKeyAuth",               // <-- reference name
        type = SecuritySchemeType.APIKEY,
        in   = SecuritySchemeIn.HEADER,
        paramName = "X-API-Key"
)
public class OpenApiConfig {
    @Bean
    OpenAPI customiseApi() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"));
    }
}
