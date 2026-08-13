package com.servicoja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServicoJaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicoJaApiApplication.class, args);
	}

}
