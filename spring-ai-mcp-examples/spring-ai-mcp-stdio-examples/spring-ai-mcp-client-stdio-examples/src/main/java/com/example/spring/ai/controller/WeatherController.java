package com.example.spring.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mcp/client")
public class WeatherController {

	private final ChatClient deepseekChatClient;

	public WeatherController(ChatClient deepseekChatClient) {
		this.deepseekChatClient = deepseekChatClient;
	}

	@GetMapping("/getweather")
	public String getWeather(@RequestParam String question) {
		return deepseekChatClient.prompt(question).call().content();
	}
} 
