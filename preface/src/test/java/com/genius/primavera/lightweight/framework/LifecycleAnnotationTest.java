package com.genius.primavera.lightweight.framework;

import com.genius.primavera.lightweight.framework.lifecycle.MultipleLifecycleBean;
import com.genius.primavera.lightweight.framework.lifecycle.TestLifecycleBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleAnnotationTest {
    
    @Test
    @DisplayName("@PostConstruct translated_text_4 successfully calledtranslated_text_3 test")
    void shouldCallPostConstructMethod() {
        PrimaveraApplicationContext context = new PrimaveraApplicationContext(
                "com.genius.primavera.lightweight.framework.lifecycle");

        TestLifecycleBean bean = context.getBean(TestLifecycleBean.class);
        assertNotNull(bean);
        assertTrue(bean.isInitialized(), "@PostConstruct translated_text_4 calledtranslated_text_3 translated_text_1");
        assertFalse(bean.isDestroyed(), "@PreDestroy translated_text_4 translated_text_2 calledtranslated_text_2 translated_text_3 translated_text_1");

        assertEquals("translated_text_3 completed!", bean.getMessage());
    }
    
    @Test
    @DisplayName("@PreDestroy translated_text_4 successfully calledtranslated_text_3 test")
    void shouldCallPreDestroyMethod() {
        PrimaveraApplicationContext context = new PrimaveraApplicationContext(
                "com.genius.primavera.lightweight.framework.lifecycle");
        
        TestLifecycleBean bean = context.getBean(TestLifecycleBean.class);
        assertNotNull(bean);
        assertTrue(bean.isInitialized());

        context.close();

        assertTrue(bean.isDestroyed(), "@PreDestroy translated_text_4 calledtranslated_text_3 translated_text_1");
        assertEquals("translated_text_2 completed!", bean.getCleanupMessage());
    }

    @Test
    @DisplayName("translated_text_2 translated_text_2 @PostConstruct translated_text_4 translated_text_2 calledtranslated_text_3 test")
    void shouldCallMultiplePostConstructMethods() {
        PrimaveraApplicationContext context = new PrimaveraApplicationContext(
                "com.genius.primavera.lightweight.framework.lifecycle");
        
        MultipleLifecycleBean bean = context.getBean(MultipleLifecycleBean.class);
        assertNotNull(bean);

        assertTrue(bean.isFirstInitCalled(), "translated_text_1 translated_text_2 @PostConstruct translated_text_4 calledtranslated_text_3 translated_text_1");
        assertTrue(bean.isSecondInitCalled(), "translated_text_1 translated_text_2 @PostConstruct translated_text_4 calledtranslated_text_3 translated_text_1");
    }
}