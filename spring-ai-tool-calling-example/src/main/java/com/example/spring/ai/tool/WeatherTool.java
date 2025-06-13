package com.example.spring.ai.tool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
public class WeatherTool {

	@Bean("currentWeather")
	@Description("Get the weather in location")
	Function<String, String> currentWeather() {
		return new WeatherService();
	}

}

class WeatherService implements Function<String, String> {
	@Override
	public String apply(String cityName) {
		return switch (cityName) {
			case "北京" -> "晴天";
			case "广州" -> "阴天";
			default -> "未知";
		};
	}
}
