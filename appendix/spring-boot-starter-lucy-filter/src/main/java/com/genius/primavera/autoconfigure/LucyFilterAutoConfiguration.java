package com.genius.primavera.autoconfigure;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(XssEscapeServletFilter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(LucyFilterDelegatingProperties.class)
@ConditionalOnProperty(prefix = "lucy-filter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LucyFilterAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public XssEscapeServletFilter xssEscapeServletFilter() {
		log.info("Creating XssEscapeServletFilter");
		return new XssEscapeServletFilter();
	}

	@Bean
	@ConditionalOnMissingBean
	public FilterRegistrationBean<XssEscapeServletFilter> lucyFilterRegistrationBean(
			XssEscapeServletFilter xssEscapeServletFilter,
			LucyFilterDelegatingProperties properties) {
		log.info("Creating FilterRegistrationBean for Lucy Filter");
		FilterRegistrationBean<XssEscapeServletFilter> filterRegistration = new FilterRegistrationBean<>();
		filterRegistration.setFilter(xssEscapeServletFilter);
		filterRegistration.setName(properties.getName());
		filterRegistration.setOrder(properties.getOrder());
		filterRegistration.addUrlPatterns(properties.getAddUrlPatterns());
		return filterRegistration;
	}
}
