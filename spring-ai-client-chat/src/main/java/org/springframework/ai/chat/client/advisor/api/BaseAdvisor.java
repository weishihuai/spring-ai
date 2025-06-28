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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.AdvisorUtils;
import org.springframework.util.Assert;

/**
 * 实现 {@link CallAdvisor} 和 {@link StreamAdvisor} 公共功能的基础Advisor类，
 * 减少实现Advisor所需的样板代码。
 * <p>
 * 它为 {@link #adviseCall(ChatClientRequest, CallAdvisorChain)} 和
 * {@link #adviseStream(ChatClientRequest, StreamAdvisorChain)} 方法提供了默认实现，
 * 将实际逻辑委托给 {@link #before(ChatClientRequest, AdvisorChain advisorChain)} 和
 * {@link #after(ChatClientResponse, AdvisorChain advisorChain)} 方法。
 *
 * @author Thomas Vitale
 * @since 1.0.0
 */
public interface BaseAdvisor extends CallAdvisor, StreamAdvisor {

	Scheduler DEFAULT_SCHEDULER = Schedulers.boundedElastic();

	@Override
	default ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		Assert.notNull(chatClientRequest, "chatClientRequest cannot be null");
		Assert.notNull(callAdvisorChain, "callAdvisorChain cannot be null");

		ChatClientRequest processedChatClientRequest = before(chatClientRequest, callAdvisorChain);
		ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(processedChatClientRequest);
		return after(chatClientResponse, callAdvisorChain);
	}

	@Override
	default Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		Assert.notNull(chatClientRequest, "chatClientRequest cannot be null");
		Assert.notNull(streamAdvisorChain, "streamAdvisorChain cannot be null");
		Assert.notNull(getScheduler(), "scheduler cannot be null");

		Flux<ChatClientResponse> chatClientResponseFlux = Mono.just(chatClientRequest)
			.publishOn(getScheduler())
			.map(request -> this.before(request, streamAdvisorChain))
			.flatMapMany(streamAdvisorChain::nextStream);

		return chatClientResponseFlux.map(response -> {
			if (AdvisorUtils.onFinishReason().test(response)) {
				response = after(response, streamAdvisorChain);
			}
			return response;
		}).onErrorResume(error -> Flux.error(new IllegalStateException("Stream processing failed", error)));
	}

	@Override
	default String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 在调用其余的Advisor链之前执行的逻辑。
	 */
	ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain);

	/**
	 * 在调用其余的Advisor链之后执行的逻辑。
	 */
	ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain);

	/**
	 * 流式处理时用于处理Advisor逻辑的调度器。
	 */
	default Scheduler getScheduler() {
		return DEFAULT_SCHEDULER;
	}

}
