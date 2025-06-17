package com.example.spring.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mcp/client")
public class WeatherController {

	private final ChatClient deepseekChatClient;

	private final ToolCallbackProvider tools;

	public WeatherController(ChatClient deepseekChatClient, ToolCallbackProvider tools) {
		this.deepseekChatClient = deepseekChatClient;
		this.tools = tools;
	}

	@GetMapping("/getweather")
	public String getWeather(@RequestParam String question) {
		return deepseekChatClient.prompt(question)
				.toolCallbacks(tools)
				.call()
				.content();
	}
} 
