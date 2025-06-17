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

import io.modelcontextprotocol.spec.McpClientTransport;

/**
 * 一个命名的MCP客户端传输。通常由传输自动配置创建，但也可以手动创建它们。
 *
 * @param name 传输的名称。通常是服务器连接的名称。
 * @param transport MCP客户端传输。
 * @author Christian Tzolov
 * @since 1.0.0
 */
public record NamedClientMcpTransport(String name, McpClientTransport transport) {

}
