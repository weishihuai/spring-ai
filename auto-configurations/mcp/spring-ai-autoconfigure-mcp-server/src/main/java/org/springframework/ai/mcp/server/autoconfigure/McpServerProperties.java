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

package org.springframework.ai.mcp.server.autoconfigure;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * Model Context Protocol (MCP) 服务器的配置属性。
 * <p>
 * 这些属性控制 MCP 服务器的行为和配置，包括：
 * <ul>
 * <li>服务器标识（名称和版本）</li>
 * <li>工具、资源和提示的变化通知设置</li>
 * <li>Web 传输端点配置</li>
 * </ul>
 * <p>
 * 所有属性都以 {@code spring.ai.mcp.server} 为前缀。
 *
 * @author Christian Tzolov
 * @since 1.0.0
 * @see McpServerAutoConfiguration
 */
@ConfigurationProperties(McpServerProperties.CONFIG_PREFIX)
public class McpServerProperties {

    public static final String CONFIG_PREFIX = "spring.ai.mcp.server";

    /**
     * 启用/禁用 MCP 服务器。
     * <p>
     * 当设置为 false 时，MCP 服务器及其所有组件将不会被初始化。
     */
    private boolean enabled = true;

    /**
     * 启用/禁用标准输入/输出（stdio）传输。
     * <p>
     * 当启用时，服务器将监听标准输入上的传入消息，并将响应写入标准输出。
     */
    private boolean stdio = false;

    /**
     * MCP 服务器实例的名称。
     * <p>
     * 该名称用于在日志和监控中标识服务器。
     */
    private String name = "mcp-server";

    /**
     * MCP 服务器实例的版本。
     * <p>
     * 该版本报告给客户端，并用于兼容性检查。
     */
    private String version = "1.0.0";

    /**
     * MCP 服务器实例的说明。
     * <p>
     * 这些说明用于向客户端提供如何与该服务器交互的指导。
     */
    private String instructions = null;

    /**
     * 启用/禁用资源变化通知。仅对具有资源能力的 MCP 服务器相关。
     * <p>
     * 当启用时，服务器将在资源添加、更新或删除时通知客户端。
     */
    private boolean resourceChangeNotification = true;

    /**
     * 启用/禁用工具变化通知。仅对具有工具能力的 MCP 服务器相关。
     * <p>
     * 当启用时，服务器将在工具注册或注销时通知客户端。
     */
    private boolean toolChangeNotification = true;

    /**
     * 启用/禁用提示变化通知。仅对具有提示能力的 MCP 服务器相关。
     * <p>
     * 当启用时，服务器将在提示模板修改时通知客户端。
     */
    private boolean promptChangeNotification = true;

    /**
     * 基础 URL。
     */
    private String baseUrl = "";

    /**
     * 使用 Web 传输时的 Server-Sent Events (SSE) 端点路径。
     * <p>
     * 该属性仅在传输设置为 WEBMVC 或 WEBFLUX 时使用。
     */
    private String sseEndpoint = "/sse";

    /**
     * 使用 Web 传输时的 Server-Sent Events (SSE) 消息端点路径。
     * <p>
     * 该属性仅在传输设置为 WEBMVC 或 WEBFLUX 时使用。
     */
    private String sseMessageEndpoint = "/mcp/message";

    /**
     * 用于 MCP 服务器通信的服务器类型。
     * <p>
     * 支持的类型包括：
     * <ul>
     * <li>SYNC - 标准同步服务器（默认）</li>
     * <li>ASYNC - 异步服务器</li>
     * </ul>
     */
    private ServerType type = ServerType.SYNC;

    private Capabilities capabilities = new Capabilities();

    /**
     * 设置等待服务器响应的超时时间，以超时请求。此超时适用于通过客户端发出的所有请求，包括工具调用、资源访问和提示操作。
     */
    private Duration requestTimeout = Duration.ofSeconds(20);

    public Duration getRequestTimeout() {
        return this.requestTimeout;
    }

    public void setRequestTimeout(Duration requestTimeout) {
        Assert.notNull(requestTimeout, "Request timeout must not be null");
        this.requestTimeout = requestTimeout;
    }

    public Capabilities getCapabilities() {
        return this.capabilities;
    }

    /**
     * MCP 服务器支持的服务器类型。
     */
    public enum ServerType {

        /**
         * 同步 (McpSyncServer) 服务器
         */
        SYNC,

        /**
         * 异步 (McpAsyncServer) 服务器
         */
        ASYNC

    }

    /**
     * （可选）每个工具名称的响应 MIME 类型。
     */
    private Map<String, String> toolResponseMimeType = new HashMap<>();

    public boolean isStdio() {
        return this.stdio;
    }

    public void setStdio(boolean stdio) {
        this.stdio = stdio;
    }

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
        Assert.hasText(name, "Name must not be empty");
        this.name = name;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        Assert.hasText(version, "Version must not be empty");
        this.version = version;
    }

    public String getInstructions() {
        return this.instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public boolean isResourceChangeNotification() {
        return this.resourceChangeNotification;
    }

    public void setResourceChangeNotification(boolean resourceChangeNotification) {
        this.resourceChangeNotification = resourceChangeNotification;
    }

    public boolean isToolChangeNotification() {
        return this.toolChangeNotification;
    }

    public void setToolChangeNotification(boolean toolChangeNotification) {
        this.toolChangeNotification = toolChangeNotification;
    }

    public boolean isPromptChangeNotification() {
        return this.promptChangeNotification;
    }

    public void setPromptChangeNotification(boolean promptChangeNotification) {
        this.promptChangeNotification = promptChangeNotification;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        Assert.notNull(baseUrl, "Base URL must not be null");
        this.baseUrl = baseUrl;
    }

    public String getSseEndpoint() {
        return this.sseEndpoint;
    }

    public void setSseEndpoint(String sseEndpoint) {
        Assert.hasText(sseEndpoint, "SSE endpoint must not be empty");
        this.sseEndpoint = sseEndpoint;
    }

    public String getSseMessageEndpoint() {
        return this.sseMessageEndpoint;
    }

    public void setSseMessageEndpoint(String sseMessageEndpoint) {
        Assert.hasText(sseMessageEndpoint, "SSE message endpoint must not be empty");
        this.sseMessageEndpoint = sseMessageEndpoint;
    }

    public ServerType getType() {
        return this.type;
    }

    public void setType(ServerType serverType) {
        Assert.notNull(serverType, "Server type must not be null");
        this.type = serverType;
    }

    public Map<String, String> getToolResponseMimeType() {
        return this.toolResponseMimeType;
    }

    public static class Capabilities {

        private boolean resource = true;

        private boolean tool = true;

        private boolean prompt = true;

        private boolean completion = true;

        /**
         * 是否支持资源。
         */
        public boolean isResource() {
            return this.resource;
        }

        /**
         * 设置是否支持资源。
         */
        public void setResource(boolean resource) {
            this.resource = resource;
        }

        /**
         * 是否支持工具。
         */
        public boolean isTool() {
            return this.tool;
        }

        /**
         * 设置是否支持工具。
         */
        public void setTool(boolean tool) {
            this.tool = tool;
        }

        /**
         * 是否支持提示。
         */
        public boolean isPrompt() {
            return this.prompt;
        }

        /**
         * 设置是否支持提示。
         */
        public void setPrompt(boolean prompt) {
            this.prompt = prompt;
        }

        /**
         * 是否支持补全。
         */
        public boolean isCompletion() {
            return this.completion;
        }

        /**
         * 设置是否支持补全。
         */
        public void setCompletion(boolean completion) {
            this.completion = completion;
        }

    }

}
