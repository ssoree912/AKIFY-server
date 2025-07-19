package com.jammering.akkipy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        String securityJwtName = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("JWT");
        Components components = new Components().addSecuritySchemes(securityJwtName, new SecurityScheme()
                .name(securityJwtName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("Bearer")
                .bearerFormat(securityJwtName));


        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components)
                .info(apiInfo());
    }
    @Bean
    public GroupedOpenApi group1() {
        return GroupedOpenApi.builder()
                .group("client")
                .pathsToMatch("/api/v1/**")
                // .packagesToScan("com.example.swagger") // package 필터 설정
                .build();
    }

    private Info apiInfo() {
        return new Info()
                .title("Akify 서버 api")
                .description("akify 서버 api 문서입니다.")
                .version("1.0.0");
    }

}