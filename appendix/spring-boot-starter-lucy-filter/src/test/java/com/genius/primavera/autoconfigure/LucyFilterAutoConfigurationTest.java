package com.genius.primavera.autoconfigure;

import com.navercorp.lucy.security.xss.servletfilter.XssEscapeServletFilter;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Lucy Filter 자동 설정 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LucyFilterAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
					LucyFilterAutoConfiguration.class
			));

	@Test
	@Order(1)
	@DisplayName("자동 설정이 활성화되면 XssEscapeServletFilter Bean이 생성된다")
	void shouldCreateXssEscapeServletFilterBean() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(XssEscapeServletFilter.class);
			assertThat(context).hasSingleBean(FilterRegistrationBean.class);
		});
	}

	@Test
	@Order(2)
	@DisplayName("spring.lucy-filter.enabled=false일 때 자동 설정이 비활성화된다")
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
	@DisplayName("커스텀 프로퍼티 설정이 적용된다")
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
	@DisplayName("기본 프로퍼티 값이 적용된다")
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
	@DisplayName("사용자가 직접 XssEscapeServletFilter Bean을 정의하면 자동 설정이 동작하지 않는다")
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