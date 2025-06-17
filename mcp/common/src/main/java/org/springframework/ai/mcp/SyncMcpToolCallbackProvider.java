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

import java.util.List;
import java.util.function.BiPredicate;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * {@link ToolCallbackProvider} 的实现，用于从一个或多个 MCP 服务器发现并提供工具。
 * <p>
 * 该类作为 Spring AI 的工具提供者，能够自动从多个 MCP 服务器发现工具，并将其注册为 Spring AI 工具。其主要功能包括：
 * <ul>
 * <li>通过同步客户端连接到一个或多个MCP服务器</li>
 * <li>列出并检索所有连接服务器上的可用工具</li>
 * <li>为每个发现的工具创建{@link SyncMcpToolCallback}实例</li>
 * <li>验证工具名称以防止在所有服务器上重复</li>
 * </ul>
 * <p>
 * Example usage with a single client:
 *
 * <pre>{@code
 * McpSyncClient mcpClient = // obtain MCP client
 * ToolCallbackProvider provider = new SyncMcpToolCallbackProvider(mcpClient);
 *
 * // Get all available tools
 * ToolCallback[] tools = provider.getToolCallbacks();
 * }</pre>
 * <p>
 * Example usage with multiple clients:
 *
 * <pre>{@code
 * List<McpSyncClient> mcpClients = // obtain multiple MCP clients
 * ToolCallbackProvider provider = new SyncMcpToolCallbackProvider(mcpClients);
 *
 * // Get tools from all clients
 * ToolCallback[] tools = provider.getToolCallbacks();
 * }</pre>
 *
 * @author Christian Tzolov
 * @see ToolCallbackProvider
 * @see SyncMcpToolCallback
 * @see McpSyncClient
 * @since 1.0.0
 */

public class SyncMcpToolCallbackProvider implements ToolCallbackProvider {

	/**
	 * 用于发现工具的MCP客户端列表
	 */
	private final List<McpSyncClient> mcpClients;

	/**
	 * 应用于发现工具的过滤器
	 */
	private final BiPredicate<McpSyncClient, Tool> toolFilter;

	/**
	 * Creates a new {@code SyncMcpToolCallbackProvider} instance with a list of MCP
	 * clients.
	 * @param mcpClients the list of MCP clients to use for discovering tools
	 * @param toolFilter a filter to apply to each discovered tool
	 */
	public SyncMcpToolCallbackProvider(BiPredicate<McpSyncClient, Tool> toolFilter, List<McpSyncClient> mcpClients) {
		Assert.notNull(mcpClients, "MCP clients must not be null");
		Assert.notNull(toolFilter, "Tool filter must not be null");
		this.mcpClients = mcpClients;
		this.toolFilter = toolFilter;
	}

	/**
	 * Creates a new {@code SyncMcpToolCallbackProvider} instance with a list of MCP
	 * clients.
	 * @param mcpClients the list of MCP clients to use for discovering tools
	 */
	public SyncMcpToolCallbackProvider(List<McpSyncClient> mcpClients) {
		this((mcpClient, tool) -> true, mcpClients);
	}

	/**
	 * Creates a new {@code SyncMcpToolCallbackProvider} instance with one or more MCP
	 * clients.
	 * @param mcpClients the MCP clients to use for discovering tools
	 * @param toolFilter a filter to apply to each discovered tool
	 */
	public SyncMcpToolCallbackProvider(BiPredicate<McpSyncClient, Tool> toolFilter, McpSyncClient... mcpClients) {
		this(toolFilter, List.of(mcpClients));
	}

	/**
	 * Creates a new {@code SyncMcpToolCallbackProvider} instance with one or more MCP
	 * clients.
	 * @param mcpClients the MCP clients to use for discovering tools
	 */
	public SyncMcpToolCallbackProvider(McpSyncClient... mcpClients) {
		this(List.of(mcpClients));
	}

	/**
	 * 从所有连接的MCP服务器发现并返回所有可用的工具。
	 * <p>
	 * 该方法：
	 * <ol>
	 * <li>从每个连接的MCP服务器获取工具列表</li>
	 * <li>为每个发现的工具创建一个{@link SyncMcpToolCallback}</li>
	 * <li>验证所有服务器中没有重复的工具名称</li>
	 * </ol>
	 * @return 包含每个发现工具的工具回调数组
	 * @throws IllegalStateException 如果发现重复的工具名称
	 */
	@Override
	public ToolCallback[] getToolCallbacks() {
		// 使用流操作从所有MCP客户端中获取工具
		var array = this.mcpClients.stream()
			.flatMap(mcpClient -> mcpClient.listTools()
				.tools()
				.stream()
				// 过滤工具，仅保留通过工具过滤器的工具
				.filter(tool -> this.toolFilter.test(mcpClient, tool))
				// 为每个工具创建一个SyncMcpToolCallback实例
				.map(tool -> new SyncMcpToolCallback(mcpClient, tool)))
			// 将流转换为ToolCallback数组
			.toArray(ToolCallback[]::new);
		
		// 验证工具回调，确保没有重复的工具名称
		validateToolCallbacks(array);
		
		// 返回工具回调数组
		return array;
	}

	/**
	 * Validates that there are no duplicate tool names in the provided callbacks.
	 * <p>
	 * This method ensures that each tool has a unique name, which is required for proper
	 * tool resolution and execution.
	 * @param toolCallbacks the tool callbacks to validate
	 * @throws IllegalStateException if duplicate tool names are found
	 */
	private void validateToolCallbacks(ToolCallback[] toolCallbacks) {
		List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
		if (!duplicateToolNames.isEmpty()) {
			throw new IllegalStateException(
					"Multiple tools with the same name (%s)".formatted(String.join(", ", duplicateToolNames)));
		}
	}

	/**
	 * 从多个MCP客户端创建工具回调的综合列表。
	 * <p>
	 * 这个工具方法提供了一种方便的方式来从多个MCP客户端一次性创建工具回调。它：
	 * <ol>
	 * <li>接收一个MCP客户端列表作为输入</li>
	 * <li>创建一个实例来管理所有客户端</li>
	 * <li>从所有客户端检索工具并将其合并到一个列表中</li>
	 * <li>确保不同客户端之间的工具名称没有冲突</li>
	 * </ol>
	 * @param mcpClients 要从中创建回调的MCP客户端列表
	 * @return 所有提供的客户端的工具回调列表
	 */
	public static List<ToolCallback> syncToolCallbacks(List<McpSyncClient> mcpClients) {

		if (CollectionUtils.isEmpty(mcpClients)) {
			return List.of();
		}
		// 创建一个SyncMcpToolCallbackProvider实例来管理所有MCP客户端
		// 调用getToolCallbacks方法获取所有工具回调
		return List.of((new SyncMcpToolCallbackProvider(mcpClients).getToolCallbacks()));
	}

}
