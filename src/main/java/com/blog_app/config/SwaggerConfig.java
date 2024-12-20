package com.blog_app.config;


import javax.tools.DocumentationTool.DocumentationTask;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;


@Configuration
public class SwaggerConfig {

	@Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Blog App API")
                .version("1.0")
                .description("This is a backend API documentation for the Blog App. developed by Bilal  & Madhushreeya")
                .contact(new Contact()
                    .name("Bilal Aijaz Khan , Madhushreeya Ramchandra Dhuri")
                    .email("mbk@gmail.com")
                    .url("https://www.instagram.com/_musheer18_?igsh=NWVwN2JlNDNqdzMz"))
                .license(new License()
                    .name("licence")
                    .url("/")
                )
            ).addSecurityItem(new SecurityRequirement().addList("JWT"))
            .components(new io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes("JWT", new SecurityScheme()
                        .name("JWT")
                        .type(Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
	
}