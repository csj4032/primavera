package com.genius.primavera.basics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Bean Scope 테스트")
class BeanScopeExampleTest {
    
    @Autowired
    private ApplicationContext context;
    
    @Test
    @DisplayName("Singleton Bean은 항상 같은 인스턴스를 반환")
    void testSingletonScope() {
        var bean1 = context.getBean(BeanScopeExample.SingletonBean.class);
        var bean2 = context.getBean(BeanScopeExample.SingletonBean.class);
        assertThat(bean1).isSameAs(bean2);
        assertThat(bean1.getId()).isEqualTo(bean2.getId());
    }
    
    @Test
    @DisplayName("Prototype Bean은 매번 새로운 인스턴스를 생성")
    void testPrototypeScope() {
        var bean1 = context.getBean(BeanScopeExample.PrototypeBean.class);
        var bean2 = context.getBean(BeanScopeExample.PrototypeBean.class);
        assertThat(bean1).isNotSameAs(bean2);
        assertThat(bean1.getId()).isNotEqualTo(bean2.getId());
    }
}