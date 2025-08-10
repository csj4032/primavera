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
    @DisplayName("Singleton Beantranslated_text_1 translated_text_2 translated_text_1 translated_text_5 translated_text_2")
    void testSingletonScope() {
        var bean1 = context.getBean(BeanScopeExample.SingletonBean.class);
        var bean2 = context.getBean(BeanScopeExample.SingletonBean.class);
        assertThat(bean1).isSameAs(bean2);
        assertThat(bean1.getId()).isEqualTo(bean2.getId());
    }
    
    @Test
    @DisplayName("Prototype Beantranslated_text_1 translated_text_2 translated_text_3 translated_text_5 creation")
    void testPrototypeScope() {
        var bean1 = context.getBean(BeanScopeExample.PrototypeBean.class);
        var bean2 = context.getBean(BeanScopeExample.PrototypeBean.class);
        assertThat(bean1).isNotSameAs(bean2);
        assertThat(bean1.getId()).isNotEqualTo(bean2.getId());
    }
}