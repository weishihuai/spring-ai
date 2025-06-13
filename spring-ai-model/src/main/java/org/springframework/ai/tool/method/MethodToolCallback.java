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

package org.springframework.ai.tool.method;

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.execution.DefaultToolCallResultConverter;
import org.springframework.ai.tool.execution.ToolCallResultConverter;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Stream;

/**
 * {@link ToolCallback}实现调用方法作为工具。
 *
 * @author Thomas Vitale
 * @since 1.0.0
 */
public final class MethodToolCallback implements ToolCallback {

	private static final Logger logger = LoggerFactory.getLogger(MethodToolCallback.class);

	private static final ToolCallResultConverter DEFAULT_RESULT_CONVERTER = new DefaultToolCallResultConverter();

	private static final ToolMetadata DEFAULT_TOOL_METADATA = ToolMetadata.builder().build();

	private final ToolDefinition toolDefinition;

	private final ToolMetadata toolMetadata;

	private final Method toolMethod;

	@Nullable
	private final Object toolObject;

	private final ToolCallResultConverter toolCallResultConverter;

	public MethodToolCallback(ToolDefinition toolDefinition, @Nullable ToolMetadata toolMetadata, Method toolMethod,
			@Nullable Object toolObject, @Nullable ToolCallResultConverter toolCallResultConverter) {
		Assert.notNull(toolDefinition, "toolDefinition cannot be null");
		Assert.notNull(toolMethod, "toolMethod cannot be null");
		Assert.isTrue(Modifier.isStatic(toolMethod.getModifiers()) || toolObject != null,
				"toolObject cannot be null for non-static methods");
		this.toolDefinition = toolDefinition;
		this.toolMetadata = toolMetadata != null ? toolMetadata : DEFAULT_TOOL_METADATA;
		this.toolMethod = toolMethod;
		this.toolObject = toolObject;
		this.toolCallResultConverter = toolCallResultConverter != null ? toolCallResultConverter
				: DEFAULT_RESULT_CONVERTER;
	}

	@Override
	public ToolDefinition getToolDefinition() {
		return this.toolDefinition;
	}

	@Override
	public ToolMetadata getToolMetadata() {
		return this.toolMetadata;
	}

	@Override
	public String call(String toolInput) {
		return call(toolInput, null);
	}

	/**
	 * 触发方法调用
	 *
	 * @param toolInput   工具输入参数
	 * @param toolContext 工具上下文
	 * @return 工具输出结果
	 */
	@Override
	public String call(String toolInput, @Nullable ToolContext toolContext) {
		Assert.hasText(toolInput, "toolInput cannot be null or empty");

		logger.debug("Starting execution of tool: {}", this.toolDefinition.name());

		validateToolContextSupport(toolContext);

		// 将模型处理后的字符串文本，转化为对应的输入模式
		Map<String, Object> toolArguments = extractToolArguments(toolInput);

		Object[] methodArguments = buildMethodArguments(toolArguments, toolContext);

		// 调用工具的方法 + 输入参数，得到工具的输出结果
		Object result = callMethod(methodArguments);

		logger.debug("Successful execution of tool: {}", this.toolDefinition.name());

		// 获取工具方法的返回类型
		Type returnType = this.toolMethod.getGenericReturnType();

		// 将工具的输出结果，转化为指定的返回类型
		return this.toolCallResultConverter.convert(result, returnType);
	}

	private void validateToolContextSupport(@Nullable ToolContext toolContext) {
		var isNonEmptyToolContextProvided = toolContext != null && !CollectionUtils.isEmpty(toolContext.getContext());
		var isToolContextAcceptedByMethod = Stream.of(this.toolMethod.getParameterTypes())
			.anyMatch(type -> ClassUtils.isAssignable(type, ToolContext.class));
		if (isToolContextAcceptedByMethod && !isNonEmptyToolContextProvided) {
			throw new IllegalArgumentException("ToolContext is required by the method as an argument");
		}
	}

	/**
	 * 从工具输入中提取参数并生成一个映射
	 * 该方法使用JsonParser类将给定的工具输入字符串解析为一个Map对象
	 * 这个Map对象随后被用来获取工具执行所需的参数
	 *
	 * @param toolInput 工具输入的JSON格式字符串，包含待解析的参数
	 * @return 一个包含工具参数的Map对象，其中键是参数名，值是参数值
	 */
	private Map<String, Object> extractToolArguments(String toolInput) {
		return JsonParser.fromJson(toolInput, new TypeReference<>() {
		});
	}

	// Based on the implementation in MethodToolCallback.
	private Object[] buildMethodArguments(Map<String, Object> toolInputArguments, @Nullable ToolContext toolContext) {
		return Stream.of(this.toolMethod.getParameters()).map(parameter -> {
			if (parameter.getType().isAssignableFrom(ToolContext.class)) {
				return toolContext;
			}
			Object rawArgument = toolInputArguments.get(parameter.getName());
			return buildTypedArgument(rawArgument, parameter.getParameterizedType());
		}).toArray();
	}

	@Nullable
	private Object buildTypedArgument(@Nullable Object value, Type type) {
		if (value == null) {
			return null;
		}

		if (type instanceof Class<?>) {
			return JsonParser.toTypedObject(value, (Class<?>) type);
		}

		// For generic types, use the fromJson method that accepts Type
		String json = JsonParser.toJson(value);
		return JsonParser.fromJson(json, type);
	}

	@Nullable
	private Object callMethod(Object[] methodArguments) {
		// 检查对象或方法是否不具备公共访问权限，如果是，则设置方法可访问
		if (isObjectNotPublic() || isMethodNotPublic()) {
			this.toolMethod.setAccessible(true);
		}

		Object result;
		try {
			// 通过反射调用方法并传递参数，获取结果
			result = this.toolMethod.invoke(this.toolObject, methodArguments);
		}
		catch (IllegalAccessException ex) {
			throw new IllegalStateException("Could not access method: " + ex.getMessage(), ex);
		}
		catch (InvocationTargetException ex) {
			throw new ToolExecutionException(this.toolDefinition, ex.getCause());
		}
		return result;
	}

	private boolean isObjectNotPublic() {
		return this.toolObject != null && !Modifier.isPublic(this.toolObject.getClass().getModifiers());
	}

	private boolean isMethodNotPublic() {
		return !Modifier.isPublic(this.toolMethod.getModifiers());
	}

	@Override
	public String toString() {
		return "MethodToolCallback{" + "toolDefinition=" + this.toolDefinition + ", toolMetadata=" + this.toolMetadata
				+ '}';
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private ToolDefinition toolDefinition;

		private ToolMetadata toolMetadata;

		private Method toolMethod;

		private Object toolObject;

		private ToolCallResultConverter toolCallResultConverter;

		private Builder() {
		}

		public Builder toolDefinition(ToolDefinition toolDefinition) {
			this.toolDefinition = toolDefinition;
			return this;
		}

		public Builder toolMetadata(ToolMetadata toolMetadata) {
			this.toolMetadata = toolMetadata;
			return this;
		}

		public Builder toolMethod(Method toolMethod) {
			this.toolMethod = toolMethod;
			return this;
		}

		public Builder toolObject(Object toolObject) {
			this.toolObject = toolObject;
			return this;
		}

		public Builder toolCallResultConverter(ToolCallResultConverter toolCallResultConverter) {
			this.toolCallResultConverter = toolCallResultConverter;
			return this;
		}

		public MethodToolCallback build() {
			return new MethodToolCallback(this.toolDefinition, this.toolMetadata, this.toolMethod, this.toolObject,
					this.toolCallResultConverter);
		}

	}

}
