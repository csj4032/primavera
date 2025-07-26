package com.genius.primavera.autoconfigure;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnClass(XssEscapeServletFilter.class)
@EnableConfigurationProperties(LucyFilterDelegatingProperties.class)
@ConditionalOnProperty(prefix = "spring.lucy-filter", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LucyFilterAutoConfiguration {

	@EnableLucyFilter
	@AutoConfigureAfter(LucyFilterAutoConfiguration.class)
	protected static class LucyFilterApplicationConfiguration {
		public LucyFilterApplicationConfiguration() {
			log.info("LucyFilterApplicationConfiguration");
		}
	}

	@Configuration
	@ConditionalOnBean(XssEscapeServletFilter.class)
	@AutoConfigureAfter(LucyFilterApplicationConfiguration.class)
	protected static class LucyFilterApplicationConfigurationAdapter {

		private XssEscapeServletFilter xssEscapeServletFilter;
		private LucyFilterDelegatingProperties lucyFilterDelegatingProperties;

		public LucyFilterApplicationConfigurationAdapter(XssEscapeServletFilter xssEscapeServletFilter, LucyFilterDelegatingProperties lucyFilterDelegatingProperties) {
			log.info("LucyFilterApplicationConfigurationAdapter");
			this.xssEscapeServletFilter = xssEscapeServletFilter;
			this.lucyFilterDelegatingProperties = lucyFilterDelegatingProperties;
			filterRegistrationBean();
		}

		private FilterRegistrationBean<XssEscapeServletFilter> filterRegistrationBean() {
			log.info("FilterRegistrationBean : {}", this);
			FilterRegistrationBean<XssEscapeServletFilter> filterRegistration = new FilterRegistrationBean<>();
			filterRegistration.setFilter(xssEscapeServletFilter);
			filterRegistration.setName(lucyFilterDelegatingProperties.getName());
			filterRegistration.setOrder(lucyFilterDelegatingProperties.getOrder());
			filterRegistration.addUrlPatterns(lucyFilterDelegatingProperties.getAddUrlPatterns());
			return filterRegistration;
		}
	}
}
