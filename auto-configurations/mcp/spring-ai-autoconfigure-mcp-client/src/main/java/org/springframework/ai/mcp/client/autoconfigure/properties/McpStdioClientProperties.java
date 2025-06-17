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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.transport.ServerParameters;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * 模型上下文协议（MCP）stdio客户端的配置属性。
 * <p>
 * 包括服务器参数、超时和连接详情。
 *
 * 它支持通过属性直接配置和通过外部资源文件配置。
 *
 * @author Christian Tzolov
 * @since 1.0.0
 */
@ConfigurationProperties(McpStdioClientProperties.CONFIG_PREFIX)
public class McpStdioClientProperties {

    public static final String CONFIG_PREFIX = "spring.ai.mcp.client.stdio";

    /**
     * 包含MCP服务器配置的资源。
     * <p>
     * 该资源应包含定义MCP服务器及其参数的JSON配置。
     */
    private Resource serversConfiguration;

    /**
     * MCP stdio连接配置的映射。
     * <p>
     * 每个条目代表一个具有特定配置参数的命名连接。
     */
    private final Map<String, Parameters> connections = new HashMap<>();

    public Resource getServersConfiguration() {
        return this.serversConfiguration;
    }

    public void setServersConfiguration(Resource stdioConnectionResources) {
        this.serversConfiguration = stdioConnectionResources;
    }

    public Map<String, Parameters> getConnections() {
        return this.connections;
    }

    private Map<String, ServerParameters> resourceToServerParameters() {
        try {
            Map<String, Map<String, Parameters>> stdioConnection = new ObjectMapper().readValue(
                    this.serversConfiguration.getInputStream(),
                    new TypeReference<Map<String, Map<String, Parameters>>>() {
                    });

            Map<String, Parameters> mcpServerJsonConfig = stdioConnection.entrySet().iterator().next().getValue();

            return mcpServerJsonConfig.entrySet().stream().collect(Collectors.toMap(kv -> kv.getKey(), kv -> {
                Parameters parameters = kv.getValue();
                return ServerParameters.builder(parameters.command())
                    .args(parameters.args())
                    .env(parameters.env())
                    .build();
            }));
        }
        catch (Exception e) {
            throw new RuntimeException("读取stdio连接资源失败", e);
        }
    }

    public Map<String, ServerParameters> toServerParameters() {
        Map<String, ServerParameters> serverParameters = new HashMap<>();
        if (this.serversConfiguration != null) {
            serverParameters.putAll(resourceToServerParameters());
        }

        for (Map.Entry<String, Parameters> entry : this.connections.entrySet()) {
            serverParameters.put(entry.getKey(), entry.getValue().toServerParameters());
        }
        return serverParameters;
    }

    /**
     * 表示MCP服务器连接参数的记录。
     * <p>
     * 包括要执行的命令、命令参数和环境变量。
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public record Parameters(
            /**
             * 要为MCP服务器执行的命令。
             */
            @JsonProperty("command") String command,
            /**
             * 命令参数列表。
             */
            @JsonProperty("args") List<String> args,
            /**
             * 环境变量，如apiKey等。
             */
            @JsonProperty("env") Map<String, String> env) {

        public ServerParameters toServerParameters() {
            return ServerParameters.builder(this.command()).args(this.args()).env(this.env()).build();
        }

    }

}
