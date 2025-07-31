package com.genius.primavera.autoconfigure;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("XSS Escape Servlet Filter 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class XssEscapeServletFilterIntegrationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                    LucyFilterAutoConfiguration.class
            ));

    @Test
    @Order(1)
    @DisplayName("웹 애플리케이션 컨텍스트에서 Bean이 올바르게 생성되는지 확인")
    void shouldCreateBeansInWebContext() {
        contextRunner
                .withPropertyValues("spring.lucy-filter.enabled=true")
                .run(context -> {
                    assertThat(context).hasBean("xssEscapeServletFilter");
                    assertThat(context).hasBean("lucyFilterRegistrationBean");
                });
    }

    @Test
    @Order(2)
    @DisplayName("비활성화 시 Bean이 생성되지 않는지 확인")
    void shouldNotCreateBeansWhenDisabled() {
        contextRunner
                .withPropertyValues("spring.lucy-filter.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean("xssEscapeServletFilter");
                    assertThat(context).doesNotHaveBean("lucyFilterRegistrationBean");
                });
    }
}