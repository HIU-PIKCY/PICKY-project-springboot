package com.picky.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){
        Info info = new Info()
            .title("PICKY API")
            .description("PICKY API 명세서")
            .version("1.0.0");

        String jwtSchemeName = "bearerAuth";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // SecuritySchemes 등록
        Components components = new Components()
            .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization"));

        return new OpenAPI()
            .info(info)
            .addServersItem(new Server().url("/"))
            .addSecurityItem(securityRequirement)
            .components(components);

    }
}
