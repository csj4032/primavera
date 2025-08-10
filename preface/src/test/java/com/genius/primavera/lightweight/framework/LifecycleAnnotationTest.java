package com.genius.primavera.lightweight.framework;

import com.genius.primavera.lightweight.framework.lifecycle.MultipleLifecycleBean;
import com.genius.primavera.lightweight.framework.lifecycle.TestLifecycleBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleAnnotationTest {
    
    @Test
    @DisplayName("@PostConstruct file successfully calledconnection test")
    void shouldCallPostConstructMethod() {
        PrimaveraApplicationContext context = new PrimaveraApplicationContext(
                "com.genius.primavera.lightweight.framework.lifecycle");

        TestLifecycleBean bean = context.getBean(TestLifecycleBean.class);
        assertNotNull(bean);
        assertTrue(bean.isInitialized(), "@PostConstruct file calledconnection should");
        assertFalse(bean.isDestroyed(), "@PreDestroy file test calledtest connection should");

        assertEquals("connection completed!", bean.getMessage());
    }
    
    @Test
    @DisplayName("@PreDestroy file successfully calledconnection test")
    void shouldCallPreDestroyMethod() {
        PrimaveraApplicationContext context = new PrimaveraApplicationContext(
                "com.genius.primavera.lightweight.framework.lifecycle");
        
        TestLifecycleBean bean = context.getBean(TestLifecycleBean.class);
        assertNotNull(bean);
        assertTrue(bean.isInitialized());

        context.close();

        assertTrue(bean.isDestroyed(), "@PreDestroy file calledconnection should");
        assertEquals("test completed!", bean.getCleanupMessage());
    }

    @Test
    @DisplayName("test @PostConstruct file test calledconnection test")
    void shouldCallMultiplePostConstructMethods() {
        PrimaveraApplicationContext context = new PrimaveraApplicationContext(
                "com.genius.primavera.lightweight.framework.lifecycle");
        
        MultipleLifecycleBean bean = context.getBean(MultipleLifecycleBean.class);
        assertNotNull(bean);

        assertTrue(bean.isFirstInitCalled(), "should test @PostConstruct file calledconnection should");
        assertTrue(bean.isSecondInitCalled(), "should test @PostConstruct file calledconnection should");
    }
}