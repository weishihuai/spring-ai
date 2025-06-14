package com.example.spring.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean
	public ChatClient deepseekChatClient(DeepSeekChatModel chatModel,
										 ToolCallbackProvider tools) {
		return ChatClient
				.builder(chatModel)
				// 注入MCP工具
				.defaultToolCallbacks(tools)
				.build();
	}

}
