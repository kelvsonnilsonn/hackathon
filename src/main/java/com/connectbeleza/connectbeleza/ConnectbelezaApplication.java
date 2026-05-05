package com.connectbeleza.connectbeleza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConnectbelezaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConnectbelezaApplication.class, args);
	}

}