package com.example.spring.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/advisor")
public class AdvisorController {

	private final ChatClient deepseekChatClient;

	@Autowired
	public AdvisorController(ChatClient deepseekChatClient) {
		this.deepseekChatClient = deepseekChatClient;
	}

	@GetMapping("/chat")
	public String chatWithoutTools(@RequestParam("message") String message) {
		return deepseekChatClient
				.prompt(message)
				.call()
				.content();
	}

}

