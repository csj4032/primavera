package com.genius.primavera.lightweight.framework;

import com.genius.primavera.lightweight.framework.lifecycle.MultipleLifecycleBean;
import com.genius.primavera.lightweight.framework.lifecycle.TestLifecycleBean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 라이프사이클 어노테이션 (@PostConstruct, @PreDestroy) 테스트
 */
class LifecycleAnnotationTest {
    
    @Test
    @DisplayName("@PostConstruct 메서드가 정상적으로 호출되는지 테스트")
    void shouldCallPostConstructMethod() {
        PrimaveraApplicationContext context = new PrimaveraApplicationContext(
                "com.genius.primavera.lightweight.framework.lifecycle");
        
        // TestLifecycleBean이 생성되고 @PostConstruct가 호출되었는지 확인
        TestLifecycleBean bean = context.getBean(TestLifecycleBean.class);
        assertNotNull(bean);
        assertTrue(bean.isInitialized(), "@PostConstruct 메서드가 호출되어야 함");
        assertFalse(bean.isDestroyed(), "@PreDestroy 메서드는 아직 호출되지 않아야 함");
        
        // 메시지가 설정되었는지 확인
        assertEquals("초기화 완료!", bean.getMessage());
    }
    
    @Test
    @DisplayName("@PreDestroy 메서드가 정상적으로 호출되는지 테스트")
    void shouldCallPreDestroyMethod() {
        PrimaveraApplicationContext context = new PrimaveraApplicationContext(
                "com.genius.primavera.lightweight.framework.lifecycle");
        
        TestLifecycleBean bean = context.getBean(TestLifecycleBean.class);
        assertNotNull(bean);
        assertTrue(bean.isInitialized());
        
        // ApplicationContext 종료
        context.close();
        
        // @PreDestroy 메서드가 호출되었는지 확인
        assertTrue(bean.isDestroyed(), "@PreDestroy 메서드가 호출되어야 함");
        assertEquals("정리 완료!", bean.getCleanupMessage());
    }
    
    
    @Test
    @DisplayName("여러 개의 @PostConstruct 메서드가 모두 호출되는지 테스트")
    void shouldCallMultiplePostConstructMethods() {
        PrimaveraApplicationContext context = new PrimaveraApplicationContext(
                "com.genius.primavera.lightweight.framework.lifecycle");
        
        MultipleLifecycleBean bean = context.getBean(MultipleLifecycleBean.class);
        assertNotNull(bean);
        
        // 두 개의 @PostConstruct 메서드가 모두 호출되었는지 확인
        assertTrue(bean.isFirstInitCalled(), "첫 번째 @PostConstruct 메서드가 호출되어야 함");
        assertTrue(bean.isSecondInitCalled(), "두 번째 @PostConstruct 메서드가 호출되어야 함");
    }
}