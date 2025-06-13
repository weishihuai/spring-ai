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

import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.util.json.JsonParser;
import org.springframework.lang.Nullable;

/**
 * {@link ToolCallResultConverter}的默认实现。
 *
 * @author Thomas Vitale
 * @since 1.0.0
 */
public final class DefaultToolCallResultConverter implements ToolCallResultConverter {

	private static final Logger logger = LoggerFactory.getLogger(DefaultToolCallResultConverter.class);

	@Override
	public String convert(@Nullable Object result, @Nullable Type returnType) {
		// 无返回类型时：返回 "Done" 的 JSON 字符串。
		if (returnType == Void.TYPE) {
			logger.debug("The tool has no return type. Converting to conventional response.");
			return JsonParser.toJson("Done");
		}
		// 处理图像结果: 将其编码为 PNG 格式并转为 Base64 字符串，再封装成带有 MIME 类型的 JSON 对象
		if (result instanceof RenderedImage) {
			final var buf = new ByteArrayOutputStream(1024 * 4);
			try {
				ImageIO.write((RenderedImage) result, "PNG", buf);
			}
			catch (IOException e) {
				return "Failed to convert tool result to a base64 image: " + e.getMessage();
			}
			final var imgB64 = Base64.getEncoder().encodeToString(buf.toByteArray());
			return JsonParser.toJson(Map.of("mimeType", "image/png", "data", imgB64));
		}
		else {
			// 默认处理：其他情况直接将结果转换为 JSON 字符串。
			logger.debug("Converting tool result to JSON.");
			return JsonParser.toJson(result);
		}
	}

}
