package com.jammering.akkipy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AkkipyApplication {

	public static void main(String[] args) {
		SpringApplication.run(AkkipyApplication.class, args);
	}

}
