package com.genius.primavera.autoconfigure;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("XSS Escape Servlet Filter test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class XssEscapeServletFilterIntegrationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
                    LucyFilterAutoConfiguration.class
            ));

    @Test
    @Order(1)
    @DisplayName("should return with Beanshould file processing verification")
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
    @DisplayName("file should Beanneeds to be added0 connection verification")
    void shouldNotCreateBeansWhenDisabled() {
        contextRunner
                .withPropertyValues("spring.lucy-filter.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean("xssEscapeServletFilter");
                    assertThat(context).doesNotHaveBean("lucyFilterRegistrationBean");
                });
    }
}