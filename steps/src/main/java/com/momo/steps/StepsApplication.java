package com.momo.steps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StepsApplication {

	public static void main(String[] args) {
		SpringApplication.run(StepsApplication.class, args);
	}

}
