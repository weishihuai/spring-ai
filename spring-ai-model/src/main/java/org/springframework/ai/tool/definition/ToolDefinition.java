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

package org.springframework.ai.tool.definition;

/**
 * AI模型用来确定何时以及如何调用工具的定义。
 *
 * @author Thomas Vitale
 * @since 1.0.0
 */
public interface ToolDefinition {

	/**
	 * 工具名称。在提供给模型的工具集中是唯一的。
	 */
	String name();

	/**
	 * AI模型使用工具描述来确定工具的功能。
	 */
	String description();

	/**
	 * 用于调用工具的参数的模式。
	 */
	String inputSchema();

	/**
	 * 创建一个默认的{@link ToolDefinition}构建器。
	 */
	static DefaultToolDefinition.Builder builder() {
		return DefaultToolDefinition.builder();
	}

}
