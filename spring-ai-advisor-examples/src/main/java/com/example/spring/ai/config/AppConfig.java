package com.example.spring.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

	@Bean
	public ChatClient deepseekChatClient(DeepSeekChatModel chatModel) {
		MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
				.maxMessages(10)
				.build();

		return ChatClient.builder(chatModel)
				.defaultAdvisors(
						// 实现打印日志的Advisor
						new SimpleLoggerAdvisor(),
						// 实现聊天记忆的Advisor
						MessageChatMemoryAdvisor.builder(chatMemory).build()
				).build();
	}

}


