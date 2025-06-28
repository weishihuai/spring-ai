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

package org.springframework.ai.chat.client.advisor;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationContext;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationConvention;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationDocumentation;
import org.springframework.ai.chat.client.advisor.observation.DefaultAdvisorObservationConvention;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.core.OrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * {@link BaseAdvisorChain} 的默认实现。被
 * {@link org.springframework.ai.chat.client.ChatClient} 使用来委托调用链中的下一个
 * {@link CallAdvisor} 或 {@link StreamAdvisor}。
 *
 * @author Christian Tzolov
 * @author Dariusz Jedrzejczyk
 * @author Thomas Vitale
 * @since 1.0.0
 */
public class DefaultAroundAdvisorChain implements BaseAdvisorChain {

	public static final AdvisorObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultAdvisorObservationConvention();

	private static final TemplateRenderer DEFAULT_TEMPLATE_RENDERER = StTemplateRenderer.builder().build();

	/**
	 * 存储原始的CallAdvisor列表
	 */
	private final List<CallAdvisor> originalCallAdvisors;

	/**
	 * 存储原始的StreamAdvisor列表
	 */
	private final List<StreamAdvisor> originalStreamAdvisors;

	/**
	 * 当前使用的CallAdvisor队列
	 */
	private final Deque<CallAdvisor> callAdvisors;

	/**
	 * 当前使用的StreamAdvisor队列
	 */
	private final Deque<StreamAdvisor> streamAdvisors;

	private final ObservationRegistry observationRegistry;

	private final TemplateRenderer templateRenderer;

	DefaultAroundAdvisorChain(ObservationRegistry observationRegistry, @Nullable TemplateRenderer templateRenderer,
			Deque<CallAdvisor> callAdvisors, Deque<StreamAdvisor> streamAdvisors) {

		Assert.notNull(observationRegistry, "the observationRegistry must be non-null");
		Assert.notNull(callAdvisors, "the callAdvisors must be non-null");
		Assert.notNull(streamAdvisors, "the streamAdvisors must be non-null");

		this.observationRegistry = observationRegistry;
		this.templateRenderer = templateRenderer != null ? templateRenderer : DEFAULT_TEMPLATE_RENDERER;
		this.callAdvisors = callAdvisors;
		this.streamAdvisors = streamAdvisors;
		this.originalCallAdvisors = List.copyOf(callAdvisors);
		this.originalStreamAdvisors = List.copyOf(streamAdvisors);
	}

	// 调用ChatClient.call()方法时，会初始化一个默认的Advisor链：DefaultAroundAdvisorChain
	public static Builder builder(ObservationRegistry observationRegistry) {
		return new Builder(observationRegistry);
	}

	/**
	 * 调用下一个CallAdvisor处理聊天请求
	 *
	 * @param chatClientRequest
	 * @return
	 */
	@Override
	public ChatClientResponse nextCall(ChatClientRequest chatClientRequest) {
		Assert.notNull(chatClientRequest, "the chatClientRequest cannot be null");

		// 没有可执行的 CallAdvisor
		if (this.callAdvisors.isEmpty()) {
			throw new IllegalStateException("No CallAdvisors available to execute");
		}

		// org.springframework.ai.chat.client.advisor.ChatModelCallAdvisor

		// 获取下一个要执行的advisor, 处理当前请求
		var advisor = this.callAdvisors.pop();

		var observationContext = AdvisorObservationContext.builder()
			.advisorName(advisor.getName())
			.chatClientRequest(chatClientRequest)
			.order(advisor.getOrder())
			.build();

		// 创建一个观测器，用于记录和追踪 advisor 的执行过程
		return AdvisorObservationDocumentation.AI_ADVISOR
			.observation(null, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext, this.observationRegistry)
			.observe(() -> advisor.adviseCall(chatClientRequest, this));
	}

	@Override
	public Flux<ChatClientResponse> nextStream(ChatClientRequest chatClientRequest) {
		Assert.notNull(chatClientRequest, "the chatClientRequest cannot be null");

		return Flux.deferContextual(contextView -> {
			if (this.streamAdvisors.isEmpty()) {
				return Flux.error(new IllegalStateException("No StreamAdvisors available to execute"));
			}

			var advisor = this.streamAdvisors.pop();

			AdvisorObservationContext observationContext = AdvisorObservationContext.builder()
				.advisorName(advisor.getName())
				.chatClientRequest(chatClientRequest)
				.order(advisor.getOrder())
				.build();

			var observation = AdvisorObservationDocumentation.AI_ADVISOR.observation(null,
					DEFAULT_OBSERVATION_CONVENTION, () -> observationContext, this.observationRegistry);

			observation.parentObservation(contextView.getOrDefault(ObservationThreadLocalAccessor.KEY, null)).start();

			// @formatter:off
			return Flux.defer(() -> advisor.adviseStream(chatClientRequest, this)
						.doOnError(observation::error)
						.doFinally(s -> observation.stop())
						.contextWrite(ctx -> ctx.put(ObservationThreadLocalAccessor.KEY, observation)));
			// @formatter:on
		});
	}

	@Override
	public List<CallAdvisor> getCallAdvisors() {
		return this.originalCallAdvisors;
	}

	@Override
	public List<StreamAdvisor> getStreamAdvisors() {
		return this.originalStreamAdvisors;
	}

	@Override
	public ObservationRegistry getObservationRegistry() {
		return this.observationRegistry;
	}

	public static class Builder {

		// 观察注册表
		private final ObservationRegistry observationRegistry;

		// CallAdvisor队列
		private final Deque<CallAdvisor> callAdvisors;

		// StreamAdvisor队列
		private final Deque<StreamAdvisor> streamAdvisors;

		// 模板渲染器
		private TemplateRenderer templateRenderer;

		public Builder(ObservationRegistry observationRegistry) {
			this.observationRegistry = observationRegistry;
			this.callAdvisors = new ConcurrentLinkedDeque<>();
			this.streamAdvisors = new ConcurrentLinkedDeque<>();
		}

		// 设置模板渲染器
		public Builder templateRenderer(TemplateRenderer templateRenderer) {
			this.templateRenderer = templateRenderer;
			return this;
		}

		// 添加单个advisor
		public Builder push(Advisor advisor) {
			Assert.notNull(advisor, "the advisor must be non-null");
			return this.pushAll(List.of(advisor));
		}

		// 添加多个advisor
		public Builder pushAll(List<? extends Advisor> advisors) {
			Assert.notNull(advisors, "the advisors must be non-null");
			Assert.noNullElements(advisors, "the advisors must not contain null elements");
			if (!CollectionUtils.isEmpty(advisors)) {
				// 过滤并添加 CallAdvisor
				List<CallAdvisor> callAroundAdvisorList = advisors.stream()
					.filter(a -> a instanceof CallAdvisor)
					.map(a -> (CallAdvisor) a)
					.toList();

				if (!CollectionUtils.isEmpty(callAroundAdvisorList)) {
					callAroundAdvisorList.forEach(this.callAdvisors::push);
				}

				// 过滤并添加 StreamAdvisor
				List<StreamAdvisor> streamAroundAdvisorList = advisors.stream()
					.filter(a -> a instanceof StreamAdvisor)
					.map(a -> (StreamAdvisor) a)
					.toList();

				if (!CollectionUtils.isEmpty(streamAroundAdvisorList)) {
					streamAroundAdvisorList.forEach(this.streamAdvisors::push);
				}

				// 重新排序
				this.reOrder();
			}
			return this;
		}

		/**
		 * (Re)orders the advisors in priority order based on their Ordered attribute.
		 * 对当前构建器中维护的两个Advisor队列进行重新排序，确保它们按照优先级顺序执行。
		 */
		private void reOrder() {
			ArrayList<CallAdvisor> callAdvisors = new ArrayList<>(this.callAdvisors);
			// 根据每个对象实现的 Ordered 接口定义的顺序进行排序
			OrderComparator.sort(callAdvisors);
			this.callAdvisors.clear();
			callAdvisors.forEach(this.callAdvisors::addLast);

			ArrayList<StreamAdvisor> streamAdvisors = new ArrayList<>(this.streamAdvisors);
			OrderComparator.sort(streamAdvisors);
			this.streamAdvisors.clear();
			streamAdvisors.forEach(this.streamAdvisors::addLast);
		}

		// 调用ChatClient.call()方法时，会初始化一个默认的Advisor链：DefaultAroundAdvisorChain
		public DefaultAroundAdvisorChain build() {
			return new DefaultAroundAdvisorChain(this.observationRegistry, this.templateRenderer, this.callAdvisors,
					this.streamAdvisors);
		}

	}

}
