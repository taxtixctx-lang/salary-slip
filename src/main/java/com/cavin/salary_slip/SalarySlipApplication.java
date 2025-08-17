package com.cavin.salary_slip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Salary Slip Generator API",
        version = "1.0",
        description = "API for generating salary slips from Excel files with support for batch processing"
    )
)
public class SalarySlipApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalarySlipApplication.class, args);
    }
}
