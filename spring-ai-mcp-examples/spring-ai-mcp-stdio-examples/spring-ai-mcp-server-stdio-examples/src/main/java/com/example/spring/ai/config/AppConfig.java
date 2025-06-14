package com.example.spring.ai.config;

import com.example.spring.ai.service.OpenMeteoService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean
	public ToolCallbackProvider weatherTools(OpenMeteoService openMeteoService) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(openMeteoService)
				.build();
	}

	@Bean
	public ChatClient deepseekChatClient(DeepSeekChatModel chatModel) {
		return ChatClient.builder(chatModel).build();
	}

}
