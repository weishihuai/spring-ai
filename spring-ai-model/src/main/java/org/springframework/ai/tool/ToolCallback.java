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

package org.springframework.ai.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.lang.Nullable;

/**
 * 表示一个工具，其执行可以由AI模型触发。
 *
 * @author Thomas Vitale
 * @since 1.0.0
 */
public interface ToolCallback {

	/**
	 * AI模型用来确定何时以及如何调用工具的定义。
	 */
	ToolDefinition getToolDefinition();

    /**
     * 提供有关如何处理工具的附加信息的元数据。
     */
    default ToolMetadata getToolMetadata() {
        return ToolMetadata.builder().build();
    }

    /**
     * 使用给定的输入执行工具，并返回要发送回AI模型的结果。
     */
    String call(String toolInput);

    /**
     * 使用给定的输入和上下文执行工具，并返回要发送回AI模型的结果。
     */
    default String call(String toolInput, @Nullable ToolContext tooContext) {
        if (tooContext != null && !tooContext.getContext().isEmpty()) {
            throw new UnsupportedOperationException("不支持工具上下文！");
        }
        return call(toolInput);
    }

}
