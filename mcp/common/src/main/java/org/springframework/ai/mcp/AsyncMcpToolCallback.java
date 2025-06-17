/*
 * Copyright 2025-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ai.mcp;

import java.util.Map;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * 实现了 {@link ToolCallback} 接口，用于将 MCP 工具适配到 Spring AI 的工具接口，并支持异步执行。
 * <p>
 * 该类充当 Model Context Protocol (MCP) 和 Spring AI 工具系统之间的桥梁，允许 MCP 工具无缝地在 Spring AI 应用程序中使用。
 * <ul>
 * <li>将 MCP 工具定义转换为 Spring AI 工具定义</li>
 * <li>通过 MCP 客户端处理工具调用的异步执行</li>
 * <li>管理工具输入和输出的 JSON 序列化/反序列化</li>
 * </ul>
 * <p>
 * 示例用法：
 *
 * <pre>{@code
 * McpAsyncClient mcpClient = // 获取 MCP 客户端
 * Tool mcpTool = // 获取 MCP 工具定义
 * ToolCallback callback = new AsyncMcpToolCallback(mcpClient, mcpTool);
 *
 * // 通过 Spring AI 的接口使用工具
 * ToolDefinition definition = callback.getToolDefinition();
 * String result = callback.call("{\"param\": \"value\"}");
 * }</pre>
 *
 * @author Christian Tzolov
 * @see ToolCallback
 * @see McpAsyncClient
 * @see Tool
 */
public class AsyncMcpToolCallback implements ToolCallback {

	/**
	 * MCP客户端（异步）
	 */
	private final McpAsyncClient asyncMcpClient;

	/**
	 * MCP 工具定义
	 */
	private final Tool tool;

	/**
	 * 创建一个新的 {@code AsyncMcpToolCallback} 实例。
	 * @param mcpClient 用于工具执行的 MCP 客户端
	 * @param tool 要适配的 MCP 工具定义
	 */
	public AsyncMcpToolCallback(McpAsyncClient mcpClient, Tool tool) {
		this.asyncMcpClient = mcpClient;
		this.tool = tool;
	}

	/**
	 * 返回从 MCP 工具适配的 Spring AI 工具定义。
	 * <p>
	 * 工具定义包括：
	 * <ul>
	 * <li>来自 MCP 定义的工具名称</li>
	 * <li>来自 MCP 定义的工具描述</li>
	 * <li>转换为 JSON 格式的输入模式</li>
	 * </ul>
	 * @return Spring AI 工具定义
	 */
	@Override
	public ToolDefinition getToolDefinition() {
		return DefaultToolDefinition.builder()
			.name(McpToolUtils.prefixedToolName(this.asyncMcpClient.getClientInfo().name(), this.tool.name()))
			.description(this.tool.description())
			.inputSchema(ModelOptionsUtils.toJsonString(this.tool.inputSchema()))
			.build();
	}

	/**
	 * 使用提供的输入异步执行工具。
	 * <p>
	 * 该方法：
	 * <ol>
	 * <li>将 JSON 输入字符串转换为参数映射</li>
	 * <li>通过 MCP 客户端异步调用工具</li>
	 * <li>将工具响应内容转换为 JSON 字符串</li>
	 * </ol>
	 * @param functionInput 工具输入作为 JSON 字符串
	 * @return 工具响应作为 JSON 字符串
	 */
	@Override
	public String call(String functionInput) {
		Map<String, Object> arguments = ModelOptionsUtils.jsonToMap(functionInput);
		// 注意，这里使用原始工具名称，而不是来自 getToolDefinition 的适配名称
		return this.asyncMcpClient.callTool(new CallToolRequest(this.tool.name(), arguments)).map(response -> {
			if (response.isError() != null && response.isError()) {
				throw new IllegalStateException("Error calling tool: " + response.content());
			}
			return ModelOptionsUtils.toJsonString(response.content());
		}).block();
	}

	@Override
	public String call(String toolArguments, ToolContext toolContext) {
		// MCP 工具不支持 ToolContext
		return this.call(toolArguments);
	}

}
