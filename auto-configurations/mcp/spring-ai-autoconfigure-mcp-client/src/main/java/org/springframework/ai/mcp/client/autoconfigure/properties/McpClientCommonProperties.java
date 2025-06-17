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

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 适用于所有传输类型的模型上下文协议（MCP）客户端的公共配置属性。
 *
 * @author Christian Tzolov
 * @author Yangki Zhang
 * @since 1.0.0
 */
@ConfigurationProperties(McpClientCommonProperties.CONFIG_PREFIX)
public class McpClientCommonProperties {

	public static final String CONFIG_PREFIX = "spring.ai.mcp.client";

	/**
	 * 启用/禁用 MCP 客户端。
	 * <p>
	 * 当设置为 false 时，MCP 客户端及其所有组件将不会被初始化。
	 */
	private boolean enabled = true;

	/**
	 * MCP 客户端实例的名称。
	 * <p>
	 * 该名称会被报告给客户端，并用于兼容性检查。
	 */
	private String name = "spring-ai-mcp-client";

	/**
	 * MCP 客户端实例的版本。
	 * <p>
	 * 该版本会被报告给客户端，并用于兼容性检查。
	 */
	private String version = "1.0.0";

	/**
	 * 指示是否需要初始化 MCP 客户端。
	 */
	private boolean initialized = true;

	/**
	 * MCP 客户端请求的超时时间。
	 * <p>
	 * 默认值为 20 秒。
	 */
	private Duration requestTimeout = Duration.ofSeconds(20);

	/**
	 * 用于 MCP 客户端通信的客户端类型。
	 * <p>
	 * 支持的类型包括：
	 * <ul>
	 * <li>SYNC - 标准同步客户端（默认）</li>
	 * <li>ASYNC - 异步客户端</li>
	 * </ul>
	 */
	private ClientType type = ClientType.SYNC;

	/**
	 * MCP 客户端支持的客户端类型。
	 */
	public enum ClientType {

    /**
     * 同步 (McpSyncClient) 客户端
     */
    SYNC,

    /**
     * 异步 (McpAsyncClient) 客户端
     */
    ASYNC

}

	/**
	 * 启用/禁用根配置更改通知。
	 * <p>
	 * 当启用时，客户端将收到根配置更改的通知。
	 * 默认值为 true。
	 */
	private boolean rootChangeNotification = true;

	/**
	 * 工具回调配置。
	 * <p>
	 * 该配置用于启用或禁用 MCP 客户端中的工具回调。
	 */
	private Toolcallback toolcallback = new Toolcallback();

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isInitialized() {
		return this.initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public Duration getRequestTimeout() {
		return this.requestTimeout;
	}

	public void setRequestTimeout(Duration requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public ClientType getType() {
		return this.type;
	}

	public void setType(ClientType type) {
		this.type = type;
	}

	public boolean isRootChangeNotification() {
		return this.rootChangeNotification;
	}

	public void setRootChangeNotification(boolean rootChangeNotification) {
		this.rootChangeNotification = rootChangeNotification;
	}

	public Toolcallback getToolcallback() {
		return this.toolcallback;
	}

	public void setToolcallback(Toolcallback toolcallback) {
		this.toolcallback = toolcallback;
	}

	/**
	 * 表示工具的回调配置。
	 *
	 * @param enabled 指示工具回调是否启用。如果为 true，则工具回调处于活动状态；否则，它将被禁用。
	 */
	public static class Toolcallback {

    /**
     * 布尔标志，指示工具回调是否启用。如果为 true，则工具回调处于活动状态；否则，它将被禁用。
     */
    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

}

}
