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

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;

/**
 * {@link CallAdvisor} 实例的链式调用接口，用于在链中的下一个 {@link CallAdvisor} 上执行
 * {@link ChatClientRequest}。
 *
 * @author Christian Tzolov
 * @author Dariusz Jedrzejczyk
 * @author Thomas Vitale
 * @since 1.0.0
 */
public interface CallAdvisorChain extends AdvisorChain {

	/**
	 * 使用给定的请求调用链中下一个 {@link CallAdvisor}。
	 */
	ChatClientResponse nextCall(ChatClientRequest chatClientRequest);

	/**
	 * 返回在链创建时包含的所有 {@link CallAdvisor} 实例列表。
	 */
	List<CallAdvisor> getCallAdvisors();

}