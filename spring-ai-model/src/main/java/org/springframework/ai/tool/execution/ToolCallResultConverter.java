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

package org.springframework.ai.tool.execution;

import java.lang.reflect.Type;

import org.springframework.lang.Nullable;

/**
 * 一个函数式接口，用于将工具调用结果转换为可发送回AI模型的字符串形式。
 * @author Thomas Vitale
 * @since 1.0.0
 */
@FunctionalInterface
public interface ToolCallResultConverter {

	/**
	 * 将工具返回的对象结果转换为与给定类型兼容的字符串形式。
	 */
	String convert(@Nullable Object result, @Nullable Type returnType);

}
