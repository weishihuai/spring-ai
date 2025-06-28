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

import java.util.List;

import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;

/**
 * 一组 {@link StreamAdvisor} 实例的链，用于协调下一个 {@link StreamAdvisor} 对
 * {@link ChatClientRequest} 的执行。
 *
 * @author Christian Tzolov
 * @author Dariusz Jedrzejczyk
 * @author Thomas Vitale
 * @since 1.0.0
 */
public interface StreamAdvisorChain extends AdvisorChain {

	/**
	 * 使用给定的请求调用 {@link StreamAdvisorChain} 中的下一个 {@link StreamAdvisor}。
	 */
	Flux<ChatClientResponse> nextStream(ChatClientRequest chatClientRequest);

	/**
	 * 返回创建时此链中包含的所有 {@link StreamAdvisor} 实例的列表。
	 */
	List<StreamAdvisor> getStreamAdvisors();

}
