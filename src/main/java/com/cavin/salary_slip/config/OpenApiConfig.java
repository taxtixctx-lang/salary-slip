package com.cavin.salary_slip.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI salarySlipOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Salary Slip Generator API")
                        .description("API for generating salary slips from Excel sheets")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Salary Slip Support")
                                .email("support@example.com")));
    }
}
