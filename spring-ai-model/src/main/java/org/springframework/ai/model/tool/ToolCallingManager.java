/*
 * Copyright 2023-2025 the original author or authors.
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

package org.springframework.ai.model.tool;

import java.util.List;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * 负责管理聊天模型的工具调用过程的服务。
 *
 * @author Thomas Vitale
 * @since 1.0.0
 */
public interface ToolCallingManager {

	/**
	 * 从模型的工具调用选项中解析工具定义。
	 */
	List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions chatOptions);

	/**
	 * 执行模型所要求的工具调用。
	 */
	ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse);

	/**
	 * Create a default {@link ToolCallingManager} builder.
	 */
	static DefaultToolCallingManager.Builder builder() {
		return DefaultToolCallingManager.builder();
	}

}
