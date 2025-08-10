package com.genius.primavera.basics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Bean Scope test")
class BeanScopeExampleTest {
    
    @Autowired
    private ApplicationContext context;
    
    @Test
    @DisplayName("Singleton Beantest should processing test")
    void testSingletonScope() {
        var bean1 = context.getBean(BeanScopeExample.SingletonBean.class);
        var bean2 = context.getBean(BeanScopeExample.SingletonBean.class);
        assertThat(bean1).isSameAs(bean2);
        assertThat(bean1.getId()).isEqualTo(bean2.getId());
    }
    
    @Test
    @DisplayName("Prototype Beanshould test connection Endpoint creation")
    void testPrototypeScope() {
        var bean1 = context.getBean(BeanScopeExample.PrototypeBean.class);
        var bean2 = context.getBean(BeanScopeExample.PrototypeBean.class);
        assertThat(bean1).isNotSameAs(bean2);
        assertThat(bean1.getId()).isNotEqualTo(bean2.getId());
    }
}