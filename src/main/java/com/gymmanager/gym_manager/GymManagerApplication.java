package com.gymmanager.gym_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gymmanager.gym_manager")
public class GymManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymManagerApplication.class, args);
	}

}
