package com.example.spring.ai.tool;

import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;

public class TimeTools {

	String getCurrentDateTime() {
		return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
	}

}
