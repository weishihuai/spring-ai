package com.example.spring.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tool-calling/weather")
public class WeatherController {

	private final ChatClient deepseekChatClient;

	@Autowired
	public WeatherController(ChatClient deepseekChatClient) {
		this.deepseekChatClient = deepseekChatClient;
	}

	@GetMapping("/chat/with-tools")
	public String chatWithTools(@RequestParam("message") String message) {
		return deepseekChatClient.prompt(message)
				.toolNames("currentWeather")
				.call()
				.content();
	}

}

