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

package org.springframework.ai.mcp.client.autoconfigure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;

import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties.SseParameters;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebFlux 基础的服务器发送事件 (SSE) 客户端传输的自动配置类。
 *
 * <p>
 * 该配置类设置了 SSE 基础的 WebFlux 传输所需的 Bean，当 WebFlux 在类路径上可用时，
 * 提供 MCP 客户端通信的响应式传输实现。
 *
 * <p>
 * 主要功能：
 * <ul>
 * <li>为配置的 MCP 服务器连接创建 WebFlux 基础的 SSE 传输
 * <li>配置 WebClient.Builder 用于 HTTP 客户端操作
 * <li>设置 ObjectMapper 用于 JSON 序列化/反序列化
 * <li>支持具有不同基础 URL 的多个命名服务器连接
 * </ul>
 *
 * @see WebFluxSseClientTransport
 * @see McpSseClientProperties
 */
@AutoConfiguration
@ConditionalOnClass(WebFluxSseClientTransport.class)
@EnableConfigurationProperties({ McpSseClientProperties.class, McpClientCommonProperties.class })
@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
		matchIfMissing = true)
public class SseWebFluxTransportAutoConfiguration {

	/**
	 * 创建一个 WebFlux 基础的 SSE 传输列表用于 MCP 通信。
	 *
	 * <p>
	 * 每个传输都配置了：
	 * <ul>
	 * <li>具有服务器特定基础 URL 的克隆 WebClient.Builder
	 * <li>用于 JSON 处理的 ObjectMapper
	 * <li>来自属性的服务器连接参数
	 * </ul>
	 *
	 * @param sseProperties 包含服务器配置的 SSE 客户端属性
	 * @param webClientBuilderProvider WebClient.Builder 的提供者
	 * @param objectMapperProvider ObjectMapper 的提供者，如果不可用则创建新实例
	 * @return 命名的 MCP 传输列表
	 */
	@Bean
	public List<NamedClientMcpTransport> webFluxClientTransports(McpSseClientProperties sseProperties,
			ObjectProvider<WebClient.Builder> webClientBuilderProvider,
			ObjectProvider<ObjectMapper> objectMapperProvider) {

		List<NamedClientMcpTransport> sseTransports = new ArrayList<>();

		// 获取 WebClient.Builder 提供者，如果不可用则使用默认的 WebClient.builder()
		var webClientBuilderTemplate = webClientBuilderProvider.getIfAvailable(WebClient::builder);
		// 获取 ObjectMapper 提供者，如果不可用则创建新实例
		var objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);

		// 遍历配置的服务器连接参数，为每个连接创建一个 WebFlux 基础的 SSE 传输
		for (Map.Entry<String, SseParameters> serverParameters : sseProperties.getConnections().entrySet()) {
			// 克隆 WebClient.Builder 并设置服务器的基础 URL
			var webClientBuilder = webClientBuilderTemplate.clone().baseUrl(serverParameters.getValue().url());
			// 获取 SSE 端点路径，如果未配置则使用默认值 "/sse"
			String sseEndpoint = serverParameters.getValue().sseEndpoint() != null
					? serverParameters.getValue().sseEndpoint() : "/sse";
			// 构建 WebFlux 基础的 SSE 传输
			var transport = WebFluxSseClientTransport.builder(webClientBuilder)
				.sseEndpoint(sseEndpoint)
				.objectMapper(objectMapper)
				.build();
			sseTransports.add(new NamedClientMcpTransport(serverParameters.getKey(), transport));
		}

		return sseTransports;
	}

}
