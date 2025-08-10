package com.genius.primavera.basics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Bean file test")
class BeanLifecycleExampleTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private BeanLifecycleExample beanLifecycleExample;

    @Test
    @DisplayName("Beanshould successfully created and logging verification")
    void testBeanLifecycle() {
        assertThat(beanLifecycleExample).isNotNull();
        assertThat(beanLifecycleExample.getStatus()).isEqualTo("POST_CONSTRUCT");
        beanLifecycleExample.doSomething();
        assertThat(context.getBean(BeanLifecycleExample.class)).isSameAs(beanLifecycleExample);
    }

    @Test
    @DisplayName("Beanshould file connection file should test verification")
    public void testBeanDestruction() {
        assertThat(beanLifecycleExample.getStatus()).isEqualTo("POST_CONSTRUCT");
        assertThat(context.containsBean("beanLifecycleExample")).isTrue();
        assertThat(context.getBean(BeanLifecycleExample.class)).isSameAs(beanLifecycleExample);
    }
}