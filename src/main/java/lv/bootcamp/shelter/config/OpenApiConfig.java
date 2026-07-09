package lv.bootcamp.shelter.config;

import io.swagger.v3.oas.models.info.Info;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;

public class OpenApiConfig {
    @Bean
    public OpenAPI shelterOpenAPI(){
            return new OpenAPI().info(new Info().title("Shelter Animals API").version("1.0")
                    .description("Endpoints for managing animals at the animal shelter"));



    }
}
