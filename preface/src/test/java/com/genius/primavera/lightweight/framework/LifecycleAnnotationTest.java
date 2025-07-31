package com.genius.primavera.lightweight.framework;

import com.genius.primavera.lightweight.annotations.PrimaveraComponent;
import com.genius.primavera.lightweight.annotations.PrimaveraPostConstruct;
import com.genius.primavera.lightweight.annotations.PrimaveraPreDestroy;
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
                "com.genius.primavera.lightweight.framework");
        
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
                "com.genius.primavera.lightweight.framework");
        
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
    @DisplayName("잘못된 @PostConstruct 메서드 (매개변수 있음)가 예외를 발생시키는지 테스트")
    void shouldThrowExceptionForInvalidPostConstructMethod() {
        assertThrows(RuntimeException.class, () -> {
            new PrimaveraApplicationContext("com.genius.primavera.lightweight.framework");
        });
    }
    
    @Test
    @DisplayName("여러 개의 @PostConstruct 메서드가 모두 호출되는지 테스트")
    void shouldCallMultiplePostConstructMethods() {
        PrimaveraApplicationContext context = new PrimaveraApplicationContext(
                "com.genius.primavera.lightweight.framework");
        
        MultipleLifecycleBean bean = context.getBean(MultipleLifecycleBean.class);
        assertNotNull(bean);
        
        // 두 개의 @PostConstruct 메서드가 모두 호출되었는지 확인
        assertTrue(bean.isFirstInitCalled(), "첫 번째 @PostConstruct 메서드가 호출되어야 함");
        assertTrue(bean.isSecondInitCalled(), "두 번째 @PostConstruct 메서드가 호출되어야 함");
    }
    
    // 테스트용 컴포넌트들
    @PrimaveraComponent
    public static class TestLifecycleBean {
        
        private boolean initialized = false;
        private boolean destroyed = false;
        private String message;
        private String cleanupMessage;
        
        @PrimaveraPostConstruct
        public void initialize() {
            this.initialized = true;
            this.message = "초기화 완료!";
        }
        
        @PrimaveraPreDestroy
        public void cleanup() {
            this.destroyed = true;
            this.cleanupMessage = "정리 완료!";
        }
        
        public boolean isInitialized() {
            return initialized;
        }
        
        public boolean isDestroyed() {
            return destroyed;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getCleanupMessage() {
            return cleanupMessage;
        }
    }
    
    @PrimaveraComponent
    public static class MultipleLifecycleBean {
        
        private boolean firstInitCalled = false;
        private boolean secondInitCalled = false;
        
        @PrimaveraPostConstruct
        public void firstInit() {
            this.firstInitCalled = true;
        }
        
        @PrimaveraPostConstruct
        public void secondInit() {
            this.secondInitCalled = true;
        }
        
        public boolean isFirstInitCalled() {
            return firstInitCalled;
        }
        
        public boolean isSecondInitCalled() {
            return secondInitCalled;
        }
    }
    
    @PrimaveraComponent
    public static class InvalidLifecycleBean {
        
        // 잘못된 @PostConstruct 메서드 (매개변수 있음)
        @PrimaveraPostConstruct
        public void invalidInit(String parameter) {
            // 이 메서드는 예외를 발생시켜야 함
        }
    }
}