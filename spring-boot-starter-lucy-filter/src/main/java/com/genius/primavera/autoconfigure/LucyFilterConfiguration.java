package com.genius.primavera.autoconfigure;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
public class LucyFilterConfiguration {

	@Bean
	public XssEscapeServletFilter xssEscapeServletFilter() {
		log.info("LucyFilterConfiguration");
		return new XssEscapeServletFilter();
	}
}
