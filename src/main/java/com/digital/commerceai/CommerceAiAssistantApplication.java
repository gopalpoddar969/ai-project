package com.digital.commerceai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CommerceAiAssistantApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommerceAiAssistantApplication.class, args);
	}

}
