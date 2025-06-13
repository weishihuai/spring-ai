package com.example.spring.ai.controller;

import com.example.spring.ai.tool.WeatherTool;
import com.example.spring.ai.tool.DateTimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tool-calling/time")
public class TimeController {

	private final ChatClient deepseekChatClient;

	@Autowired
	public TimeController(ChatClient deepseekChatClient) {
		this.deepseekChatClient = deepseekChatClient;
	}

	@GetMapping("/chat/without-tools")
	public String chatWithoutTools(@RequestParam("message") String message) {
		return deepseekChatClient.prompt(message)
				.call().content();
	}

	@GetMapping("/chat/with-tools")
	public String chatWithTools(@RequestParam("message") String message) {
		return deepseekChatClient.prompt(message)
				// 注册为MethodToolCallback
				// DefaultChatClientRequestSpec属性: private final List<ToolCallback> toolCallbacks = new ArrayList<>();
				.tools(new DateTimeTools())
				// 初始化Advisor链，默认会添加一个ChatModelCallAdvisor
				.call()
				// 发起模型调用，并获取响应
				.content();
	}

}

