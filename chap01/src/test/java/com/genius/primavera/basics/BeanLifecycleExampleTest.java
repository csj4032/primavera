package com.genius.primavera.basics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Bean 생명주기 테스트")
class BeanLifecycleExampleTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private BeanLifecycleExample beanLifecycleExample;

    @Test
    @DisplayName("Bean이 정상적으로 생성되고 초기화되었는지 확인")
    void testBeanLifecycle() {
        assertThat(beanLifecycleExample).isNotNull();
        assertThat(beanLifecycleExample.getStatus()).isEqualTo("POST_CONSTRUCT");
        beanLifecycleExample.doSomething();
        assertThat(context.getBean(BeanLifecycleExample.class)).isSameAs(beanLifecycleExample);
    }

    @Test
    @DisplayName("Bean의 생명주기 상태와 컨텍스트 내 존재 여부 확인")
    public void testBeanDestruction() {
        assertThat(beanLifecycleExample.getStatus()).isEqualTo("POST_CONSTRUCT");
        assertThat(context.containsBean("beanLifecycleExample")).isTrue();
        assertThat(context.getBean(BeanLifecycleExample.class)).isSameAs(beanLifecycleExample);
    }
}