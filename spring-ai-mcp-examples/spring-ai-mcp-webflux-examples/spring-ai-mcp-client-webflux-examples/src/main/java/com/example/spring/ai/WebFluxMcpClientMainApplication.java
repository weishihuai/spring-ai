package com.example.spring.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebFluxMcpClientMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebFluxMcpClientMainApplication.class, args);
	}

	@Value("${ai.user.input}")
	private String userInput;

	@Autowired
	private ChatClient chatClient;

	@Bean
	public CommandLineRunner predefinedQuestions(ConfigurableApplicationContext context) {
		return args -> {
			System.out.println("\n>>> QUESTION: " + userInput);
			System.out.println("\n>>> ASSISTANT: " + chatClient.prompt(userInput).call().content());

			context.close();
		};
	}
}
