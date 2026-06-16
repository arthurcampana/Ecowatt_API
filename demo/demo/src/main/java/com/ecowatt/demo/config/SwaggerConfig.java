package com.ecowatt.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "API com Autenticação", version = "v1", description = "Documentação da API protegida"),
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "BearerAuth")
)
@SecuritySchemes({
        @SecurityScheme(
                name = "BearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT"
        )
})
public class SwaggerConfig {
}