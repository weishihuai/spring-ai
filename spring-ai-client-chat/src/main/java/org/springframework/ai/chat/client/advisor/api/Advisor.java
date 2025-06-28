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

package org.springframework.ai.chat.client.advisor.api;

import org.springframework.core.Ordered;

/**
 * 所有Advisor的父接口。
 *
 * @author Christian Tzolov
 * @author Dariusz Jedrzejczyk
 * @see CallAdvisor
 * @see StreamAdvisor
 * @see BaseAdvisor
 * @since 1.0.0
 */
public interface Advisor extends Ordered {

	/**
	 * 默认聊天记忆优先级的常量。确保此顺序比 Spring AI 内部Advisor的优先级低（例如，优先级）。
	 * 它为用户提供了插入具有更高优先级的自己的Advisor的空间（1000个插槽）。
	 */
	int DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER = Ordered.HIGHEST_PRECEDENCE + 1000;

	/**
	 * 返回Advisor的名称。
	 *
	 * @return Advisor名称。
	 */
	String getName();

}
