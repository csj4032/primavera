package com.genius.primavera.autoconfigure;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("XSS Filter Rule 설정 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class XssEscapeServletFilterRuleTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                    LucyFilterAutoConfiguration.class
            ));

    @Test
    @Order(1)
    @DisplayName("FilterRegistrationBean 설정이 올바르게 적용되는지 확인")
    void shouldConfigureFilterRegistrationBean() {
        contextRunner
                .withPropertyValues(
                        "spring.lucy-filter.enabled=true",
                        "spring.lucy-filter.name=testFilter",
                        "spring.lucy-filter.order=5",
                        "spring.lucy-filter.add-url-patterns=/test/*,/api/*"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(FilterRegistrationBean.class);
                    FilterRegistrationBean<?> filterBean = context.getBean(FilterRegistrationBean.class);
                    assertThat(filterBean.getOrder()).isEqualTo(5);
                    assertThat(filterBean.getUrlPatterns()).containsExactly("/test/*", "/api/*");
                });
    }

    @Test
    @Order(2)
    @DisplayName("기본 설정이 올바르게 적용되는지 확인")
    void shouldApplyDefaultConfiguration() {
        contextRunner
                .withPropertyValues("spring.lucy-filter.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(FilterRegistrationBean.class);
                    FilterRegistrationBean<?> filterBean = context.getBean(FilterRegistrationBean.class);
                    assertThat(filterBean.getOrder()).isEqualTo(1);
                    assertThat(filterBean.getUrlPatterns()).containsExactly("/*");
                });
    }
}