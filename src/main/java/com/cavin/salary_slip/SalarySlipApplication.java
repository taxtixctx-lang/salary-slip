package com.cavin.salary_slip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SalarySlipApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalarySlipApplication.class, args);
	}

}
