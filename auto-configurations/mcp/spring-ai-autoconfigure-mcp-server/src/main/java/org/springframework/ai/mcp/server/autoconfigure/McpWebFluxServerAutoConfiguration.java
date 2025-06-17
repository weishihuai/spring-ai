/*
 * Copyright 2025-2025 the original author or authors.
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

package org.springframework.ai.mcp.server.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.reactive.function.server.RouterFunction;

/**
 * {@link AutoConfiguration 自动配置} 用于 MCP WebFlux 服务器传输。
 * <p>
 * 该配置类设置了 MCP 服务器的 WebFlux 特定传输组件，通过 Spring WebFlux 提供响应式服务器发送事件（SSE）通信。
 * 当以下条件满足时激活：
 * <ul>
 * <li>WebFluxSseServerTransportProvider 类在类路径上（来自 mcp-spring-webflux 依赖）</li>
 * <li>Spring WebFlux 的 RouterFunction 类可用（来自 spring-boot-starter-webflux）</li>
 * <li>{@code spring.ai.mcp.server.transport} 属性设置为 {@code WEBFLUX}</li>
 * </ul>
 * <p>
 * 该配置提供：
 * <ul>
 * <li>一个 WebFluxSseServerTransportProvider bean 用于处理响应式 SSE 通信</li>
 * <li>一个 RouterFunction bean 用于设置响应式 SSE 端点</li>
 * </ul>
 * <p>
 * 所需依赖： <pre>{@code
 * <dependency>
 *     <groupId>io.modelcontextprotocol.sdk</groupId>
 *     <artifactId>mcp-spring-webflux</artifactId>
 * </dependency>
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-webflux</artifactId>
 * </dependency>
 * }</pre>
 *
 * @author Christian Tzolov
 * @author Yanming Zhou
 * @since 1.0.0
 * @see McpServerProperties
 * @see WebFluxSseServerTransportProvider
 */
@AutoConfiguration
@ConditionalOnClass({ WebFluxSseServerTransportProvider.class })
@ConditionalOnMissingBean(McpServerTransportProvider.class)
@Conditional(McpServerStdioDisabledCondition.class)
public class McpWebFluxServerAutoConfiguration {

	/**
	 * 创建一个 WebFluxSseServerTransportProvider 实例，用于提供基于 WebFlux 的 SSE 通信能力
	 *
	 * @param objectMapperProvider
	 * @param serverProperties
	 * @return
	 */
	@Bean
	@ConditionalOnMissingBean
	public WebFluxSseServerTransportProvider webFluxTransport(ObjectProvider<ObjectMapper> objectMapperProvider,
			McpServerProperties serverProperties) {
		ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
		// this.routerFunction = RouterFunctions.route()
		// MCP Server处理SSE连接
		//			.GET(this.sseEndpoint, this::handleSseConnection)
		// MCP Server处理请求
		//			.POST(this.messageEndpoint, this::handleMessage)
		//			.build();
		return new WebFluxSseServerTransportProvider(objectMapper, serverProperties.getBaseUrl(),
				serverProperties.getSseMessageEndpoint(), serverProperties.getSseEndpoint());
	}

	/**
	 * 注册一个WebFlux 路由（RouterFunction），它是 Spring WebFlux 中用于定义 HTTP 路由的函数式接口。
	 *
	 * @param webFluxProvider
	 * @return
	 */
	@Bean
	public RouterFunction<?> webfluxMcpRouterFunction(WebFluxSseServerTransportProvider webFluxProvider) {
		// WebFlux 启动一个 HTTP 服务，并监听指定的 SSE 端点。
		return webFluxProvider.getRouterFunction();
	}

}
