package com.genius.primavera.autoconfigure;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Lucy Filter test test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LucyFilterAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
					LucyFilterAutoConfiguration.class
			));

	@Test
	@Order(1)
	@DisplayName("testshould Endpoint XssEscapeServletFilter Beanneeds to be added0")
	void shouldCreateXssEscapeServletFilterBean() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(XssEscapeServletFilter.class);
			assertThat(context).hasSingleBean(FilterRegistrationBean.class);
		});
	}

	@Test
	@Order(2)
	@DisplayName("spring.lucy-filter.enabled=falseneeds to be added testshould return")
	void shouldNotCreateBeansWhenDisabled() {
		contextRunner
				.withPropertyValues("spring.lucy-filter.enabled=false")
				.run(context -> {
					assertThat(context).doesNotHaveBean(XssEscapeServletFilter.class);
					assertThat(context).doesNotHaveBean(FilterRegistrationBean.class);
				});
	}

	@Test
	@Order(3)
	@DisplayName("connection file testshould file")
	void shouldApplyCustomProperties() {
		contextRunner
				.withPropertyValues(
						"spring.lucy-filter.name=customXssFilter",
						"spring.lucy-filter.order=10",
						"spring.lucy-filter.add-url-patterns=/api/*,/admin/*"
				)
				.run(context -> {
					assertThat(context).hasSingleBean(FilterRegistrationBean.class);
					FilterRegistrationBean<?> filterBean = context.getBean(FilterRegistrationBean.class);
					assertThat(filterBean).isNotNull();
					assertThat(filterBean.getOrder()).isEqualTo(10);
					assertThat(filterBean.getUrlPatterns()).containsExactly("/api/*", "/admin/*");
				});
	}

	@Test
	@Order(4)
	@DisplayName("test file should file")
	void shouldApplyDefaultProperties() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(FilterRegistrationBean.class);
			FilterRegistrationBean<?> filterBean = context.getBean(FilterRegistrationBean.class);
			assertThat(filterBean).isNotNull();
			assertThat(filterBean.getOrder()).isEqualTo(1);
			assertThat(filterBean.getUrlPatterns()).containsExactly("/*");
		});
	}

	@Test
	@Order(5)
	@DisplayName("processing test XssEscapeServletFilter Beanshould file testshould file connection")
	void shouldBackOffWhenUserDefinesBean() {
		contextRunner
				.withUserConfiguration(CustomFilterConfiguration.class)
				.run(context -> {
					assertThat(context).hasSingleBean(XssEscapeServletFilter.class);
					assertThat(context).hasBean("customXssEscapeServletFilter");
					assertThat(context).doesNotHaveBean("xssEscapeServletFilter");
				});
	}

	@Configuration(proxyBeanMethods = false)
	static class CustomFilterConfiguration {
		@Bean
		public XssEscapeServletFilter customXssEscapeServletFilter() {
			return new XssEscapeServletFilter();
		}
	}
}