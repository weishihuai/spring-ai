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

package org.springframework.ai.tool.metadata;

import java.lang.reflect.Method;

import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.util.Assert;

/**
 * 工具规范和执行的元数据。
 *
 * @author Thomas Vitale
 * @since 1.0.0
 */
public interface ToolMetadata {

	/**
	 * 指定工具结果是直接返回还是传递回模型
	 */
	default boolean returnDirect() {
		return false;
	}

	/**
	 * 创建默认的 {@link ToolMetadata} 构建器。
	 */
	static DefaultToolMetadata.Builder builder() {
		return DefaultToolMetadata.builder();
	}

	/**
	 * 从 {@link Method} 创建默认的 {@link ToolMetadata} 实例。
	 */
	static ToolMetadata from(Method method) {
		Assert.notNull(method, "method cannot be null");
		// 从方法上的@Tool注解中获取returnDirect属性值
		return DefaultToolMetadata.builder().returnDirect(ToolUtils.getToolReturnDirect(method)).build();
	}

}
