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

package org.springframework.ai.mcp.client.autoconfigure.properties;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置基于服务器发送事件（SSE）的MCP客户端连接的属性。
 *
 * <p>
 * 这些属性允许配置多个命名的SSE连接到MCP服务器。
 * 每个连接都配置了一个用于SSE通信的URL端点。
 *
 * <p>
 * 示例配置：<pre>
 * spring.ai.mcp.client.sse:
 *   connections:
 *     server1:
 *       url: http://localhost:8080/events
 *     server2:
 *       url: http://otherserver:8081/events
 * </pre>
 *
 * @author Christian Tzolov
 * @since 1.0.0
 * @see SseParameters
 */
@ConfigurationProperties(McpSseClientProperties.CONFIG_PREFIX)
public class McpSseClientProperties {

    public static final String CONFIG_PREFIX = "spring.ai.mcp.client.sse";

    /**
     * 命名的SSE连接配置映射。
     * <p>
     * 键表示连接名称，值包含该连接的SSE参数。
     */
    private final Map<String, SseParameters> connections = new HashMap<>();

    /**
     * 返回配置的SSE连接映射。
     * @return 连接名称到其SSE参数的映射
     */
    public Map<String, SseParameters> getConnections() {
        return this.connections;
    }

    /**
     * 配置到MCP服务器的SSE连接的参数。
     *
     * @param url 与MCP服务器进行SSE通信的URL端点
     * @param sseEndpoint MCP服务器的SSE端点
     */
    public record SseParameters(String url, String sseEndpoint) {
    }

}