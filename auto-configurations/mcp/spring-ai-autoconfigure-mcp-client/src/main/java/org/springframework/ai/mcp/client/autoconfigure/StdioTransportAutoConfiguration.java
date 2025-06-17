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

import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpStdioClientProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Model Context Protocol (MCP) 的标准输入/输出 (stdio) 传输自动配置。
 *
 * <p>
 * 该配置类设置必要的 bean，用于基于 stdio 的传输，通过标准输入和输出流与 MCP 服务器进行通信。
 *
 * <p>
 * 主要功能：
 * <ul>
 * <li>为配置的 MCP 服务器连接创建 stdio 传输
 * <li>支持具有不同参数的多个命名服务器连接
 * <li>使用服务器特定的参数配置传输
 * </ul>
 *
 * @see StdioClientTransport
 * @see McpStdioClientProperties
 */
/**
 * 自动配置类，用于启用基于标准输入输出（stdio）的 MCP 客户端传输。
 *
 * <p>该配置仅在以下条件下生效：
 * <ul>
 * <li>{@link McpSchema} 类存在于类路径中（通过 {@link ConditionalOnClass}）</li>
 * <li>{@link McpStdioClientProperties} 和 {@link McpClientCommonProperties} 被正确启用并加载为配置属性</li>
 * <li>在配置文件中启用 MCP 客户端（默认启用，可通过配置项控制）</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass({ McpSchema.class })
@EnableConfigurationProperties({ McpStdioClientProperties.class, McpClientCommonProperties.class })
@ConditionalOnProperty(prefix = McpClientCommonProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
		matchIfMissing = true)
public class StdioTransportAutoConfiguration {

	/**
	 * 创建一个基于 stdio 的 MCP 通信传输列表。
	 *
	 * <p>
	 * 每个传输都配置了：
	 * <ul>
	 * <li>来自属性的服务器特定参数
	 * <li>用于标识的唯一连接名称
	 * </ul>
	 * 
	 * @param stdioProperties 包含服务器配置的 stdio 客户端属性
	 * @return 命名的 MCP 传输列表
	 */
	@Bean
	public List<NamedClientMcpTransport> stdioTransports(McpStdioClientProperties stdioProperties) {

		List<NamedClientMcpTransport> stdioTransports = new ArrayList<>();

		// 遍历 stdioProperties 中的服务器参数，为每个服务器创建一个 StdioClientTransport 实例
		for (Map.Entry<String, ServerParameters> serverParameters : stdioProperties.toServerParameters().entrySet()) {
			var transport = new StdioClientTransport(serverParameters.getValue());
			stdioTransports.add(new NamedClientMcpTransport(serverParameters.getKey(), transport));

		}

		return stdioTransports;
	}

}
