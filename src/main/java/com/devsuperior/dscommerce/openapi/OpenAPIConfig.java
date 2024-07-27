package com.devsuperior.dscommerce.openapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@OpenAPIDefinition
public class OpenAPIConfig {

     @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
            .info(new Info()
                .title("DSCommerce")
                .version("FernandoCanabarroAhnert")
                .description("Este é um projeto que foi desenvolvido durante o Curso Spring Professional da DevSuperior"));
    }
}
