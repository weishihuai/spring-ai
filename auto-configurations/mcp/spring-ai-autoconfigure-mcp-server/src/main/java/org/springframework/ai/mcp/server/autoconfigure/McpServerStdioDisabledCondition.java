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

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * 判断是否满足启用 MCP 服务器且禁用 stdio 的条件。
 * 所有内部嵌套条件类需同时满足。
 *
 * @author YunKui Lu
 * @since 1.0.0
 */
public class McpServerStdioDisabledCondition extends AllNestedConditions {

	public McpServerStdioDisabledCondition() {
		super(ConfigurationPhase.PARSE_CONFIGURATION);
	}

	// 检查配置项 spring.ai.mcp.server.enabled 是否为 true（默认匹配）。
	@ConditionalOnProperty(prefix = McpServerProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
			matchIfMissing = true)
	static class McpServerEnabledCondition {

	}

	// 检查配置项 spring.ai.mcp.server.stdio 是否为 false（默认匹配）。
	@ConditionalOnProperty(prefix = McpServerProperties.CONFIG_PREFIX, name = "stdio", havingValue = "false",
			matchIfMissing = true)
	static class StdioDisabledCondition {

	}

}
