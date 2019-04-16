package com.genius.primavera.infrastructure;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApplicationConfiguration implements WebMvcConfigurer {

//	@Bean
//	public FilterRegistrationBean<XssEscapeServletFilter> filterRegistrationBean() {
//		FilterRegistrationBean<XssEscapeServletFilter> filterRegistration = new FilterRegistrationBean<>();
//		filterRegistration.setFilter(new XssEscapeServletFilter());
//		filterRegistration.setOrder(1);
//		filterRegistration.addUrlPatterns("/*");
//		return filterRegistration;
//	}
}