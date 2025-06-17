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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.util.Assert;
import reactor.core.publisher.Flux;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.util.CollectionUtils;

/**
 * Implementation of {@link ToolCallbackProvider} that discovers and provides MCP tools
 * asynchronously from one or more MCP servers.
 * <p>
 * This class acts as a tool provider for Spring AI, automatically discovering tools from
 * multiple MCP servers and making them available as Spring AI tools. It:
 * <ul>
 * <li>Connects to MCP servers through async clients</li>
 * <li>Lists and retrieves available tools from each server asynchronously</li>
 * <li>Creates {@link AsyncMcpToolCallback} instances for each discovered tool</li>
 * <li>Validates tool names to prevent duplicates across all servers</li>
 * </ul>
 * <p>
 * Example usage with a single client:
 *
 * <pre>{@code
 * McpAsyncClient mcpClient = // obtain MCP client
 * ToolCallbackProvider provider = new AsyncMcpToolCallbackProvider(mcpClient);
 *
 * // Get all available tools
 * ToolCallback[] tools = provider.getToolCallbacks();
 * }</pre>
 *
 * Example usage with multiple clients:
 *
 * <pre>{@code
 * List<McpAsyncClient> mcpClients = // obtain multiple MCP clients
 * ToolCallbackProvider provider = new AsyncMcpToolCallbackProvider(mcpClients);
 *
 * // Get tools from all clients
 * ToolCallback[] tools = provider.getToolCallbacks();
 *
 * // Or use the reactive API
 * Flux<ToolCallback> toolsFlux = AsyncMcpToolCallbackProvider.asyncToolCallbacks(mcpClients);
 * }</pre>
 *
 * @author Christian Tzolov
 * @since 1.0.0
 * @see ToolCallbackProvider
 * @see AsyncMcpToolCallback
 * @see McpAsyncClient
 */
public class AsyncMcpToolCallbackProvider implements ToolCallbackProvider {

	private final List<McpAsyncClient> mcpClients;

	private final BiPredicate<McpAsyncClient, Tool> toolFilter;

	/**
	 * Creates a new {@code AsyncMcpToolCallbackProvider} instance with a list of MCP
	 * clients.
	 * @param mcpClients the list of MCP clients to use for discovering tools
	 * @param toolFilter a filter to apply to each discovered tool
	 */
	public AsyncMcpToolCallbackProvider(BiPredicate<McpAsyncClient, Tool> toolFilter, List<McpAsyncClient> mcpClients) {
		Assert.notNull(mcpClients, "MCP clients must not be null");
		Assert.notNull(toolFilter, "Tool filter must not be null");
		this.mcpClients = mcpClients;
		this.toolFilter = toolFilter;
	}

	/**
	 * Creates a new {@code AsyncMcpToolCallbackProvider} instance with a list of MCP
	 * clients.
	 * @param mcpClients the list of MCP clients to use for discovering tools. Each client
	 * typically connects to a different MCP server, allowing tool discovery from multiple
	 * sources.
	 * @throws IllegalArgumentException if mcpClients is null
	 */
	public AsyncMcpToolCallbackProvider(List<McpAsyncClient> mcpClients) {
		this((mcpClient, tool) -> true, mcpClients);
	}

	/**
	 * Creates a new {@code AsyncMcpToolCallbackProvider} instance with one or more MCP
	 * clients.
	 * @param mcpClients the MCP clients to use for discovering tools
	 * @param toolFilter a filter to apply to each discovered tool
	 */
	public AsyncMcpToolCallbackProvider(BiPredicate<McpAsyncClient, Tool> toolFilter, McpAsyncClient... mcpClients) {
		this(toolFilter, List.of(mcpClients));
	}

	/**
	 * Creates a new {@code AsyncMcpToolCallbackProvider} instance with one or more MCP
	 * clients.
	 * @param mcpClients the MCP clients to use for discovering tools
	 */
	public AsyncMcpToolCallbackProvider(McpAsyncClient... mcpClients) {
		this(List.of(mcpClients));
	}

	/**
	 * 从配置的MCP服务器中发现并返回所有可用的工具。
	 * <p>
	 * 该方法：
	 * <ol>
	 * <li>异步地从每个MCP服务器获取工具列表</li>
	 * <li>为每个发现的工具创建一个{@link AsyncMcpToolCallback}</li>
	 * <li>验证所有服务器中没有重复的工具名称</li>
	 * </ol>
	 * <p>
	 * 注意：虽然底层工具发现是异步的，但此方法会阻塞，直到从所有服务器发现所有工具。
	 * @return 包含每个发现工具的工具回调数组
	 * @throws IllegalStateException 如果发现重复的工具名称
	 */
	@Override
	public ToolCallback[] getToolCallbacks() {

		// 存储所有发现的工具回调
		List<ToolCallback> toolCallbackList = new ArrayList<>();

		// 遍历每个MCP异步客户端
		for (McpAsyncClient mcpClient : this.mcpClients) {

			// 异步获取工具列表并处理响应
			ToolCallback[] toolCallbacks = mcpClient.listTools()
				.map(response -> response.tools()
					.stream()
					// 过滤工具
					.filter(tool -> this.toolFilter.test(mcpClient, tool))
					// 为每个工具创建一个AsyncMcpToolCallback实例
					.map(tool -> new AsyncMcpToolCallback(mcpClient, tool))
					.toArray(ToolCallback[]::new))
				// 阻塞等待结果
				.block();

			// 验证工具回调，确保没有重复的工具名称
			validateToolCallbacks(toolCallbacks);

			toolCallbackList.addAll(List.of(toolCallbacks));
		}

		return toolCallbackList.toArray(new ToolCallback[0]);
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
	 * Creates a reactive stream of tool callbacks from multiple MCP clients.
	 * <p>
	 * This utility method provides a reactive way to work with tool callbacks from
	 * multiple MCP clients in a single operation. It:
	 * <ol>
	 * <li>Takes a list of MCP clients as input</li>
	 * <li>Creates a provider instance to manage all clients</li>
	 * <li>Retrieves tools from all clients asynchronously</li>
	 * <li>Combines them into a single reactive stream</li>
	 * <li>Ensures there are no naming conflicts between tools from different clients</li>
	 * </ol>
	 * <p>
	 * Unlike {@link #getToolCallbacks()}, this method provides a fully reactive way to
	 * work with tool callbacks, making it suitable for non-blocking applications. Any
	 * errors during tool discovery will be propagated through the returned Flux.
	 * @param mcpClients the list of MCP clients to create callbacks from
	 * @return a Flux of tool callbacks from all provided clients
	 */
	public static Flux<ToolCallback> asyncToolCallbacks(List<McpAsyncClient> mcpClients) {
		if (CollectionUtils.isEmpty(mcpClients)) {
			return Flux.empty();
		}

		return Flux.fromArray(new AsyncMcpToolCallbackProvider(mcpClients).getToolCallbacks());
	}

}
