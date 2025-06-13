package com.example.spring.ai.controller;

import com.example.spring.ai.tool.TimeTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/tool-calling/method-tool-back/time")
public class MethodToolBackController {

	private final ChatClient deepseekChatClient;

	@Autowired
	public MethodToolBackController(ChatClient deepseekChatClient) {
		this.deepseekChatClient = deepseekChatClient;
	}

	@GetMapping("/chat/with-tools")
	public String chatWithTools(@RequestParam("message") String message) {
		Method method = ReflectionUtils.findMethod(TimeTools.class, "getCurrentDateTime");

		// TODO: 2025/6/13 wsh 测试MethodToolCallback
		ToolCallback toolCallback = MethodToolCallback.builder()
				.toolDefinition(ToolDefinition.builder()
						.name("getCurrentDateTime")
						.description("Get the current date and time in the user's timezone")
						.inputSchema(JsonSchemaGenerator.generateForMethodInput(method))
						.build())
				.toolMethod(method)
				.toolObject(new TimeTools())
				.build();

		return deepseekChatClient.prompt(message)
				.tools(toolCallback)
				.call()
				.content();
	}

}

