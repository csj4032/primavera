package com.genius.primavera.testcontainer.v3;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.AnnotationUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JUnit 5 Extension for TestContainer management (v3)
 * 
 * <p>테스트 클래스 단위로 컨테이너를 관리하며, 병렬 실행 시에도 안전하게 동작합니다.</p>
 */
@Slf4j
public class TestContainerExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePreConstructCallback {
    
    private static final String CONTAINER_MANAGER_KEY = "testcontainer.manager.v3";
    private static final ExtensionContext.Namespace NAMESPACE = 
            ExtensionContext.Namespace.create(TestContainerExtension.class);
    
    // 테스트 클래스별 락 (병렬 실행 시 동기화)
    private static final ConcurrentHashMap<String, ReentrantLock> CLASS_LOCKS = 
            new ConcurrentHashMap<>();
    
    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) throws Exception {
        // TestInstancePreConstructCallback는 Spring 컨텍스트 초기화보다 먼저 실행됨
        setupContainerManager(context); 
    }
    
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // beforeAll에서는 컨테이너 시작만 처리
        startContainersIfNeeded(context);
    }
    
    private void setupContainerManager(ExtensionContext context) throws Exception {
        Class<?> testClass = context.getRequiredTestClass();
        String testClassName = testClass.getName();
        
        // 클래스별 락 획득 (병렬 실행 시 동기화)
        ReentrantLock lock = CLASS_LOCKS.computeIfAbsent(testClassName, k -> new ReentrantLock());
        lock.lock();
        
        try {
            EnableTestContainer annotation = AnnotationUtils
                    .findAnnotation(testClass, EnableTestContainer.class)
                    .orElse(null);
                    
            if (annotation != null) {
                log.info("Starting containers for test class: {}", testClassName);
                
                ContainerManager manager = getOrCreateContainerManager(context, annotation, testClass);
                
                // ThreadLocal에 설정 (Spring Context Initializer에서 사용)
                ContainerManagerHolder.set(manager);
                
                manager.startContainers();
                
                log.info("All containers started for test class: {}", testClassName);
            }
        } finally {
            lock.unlock();
        }
    }
    
    private void startContainersIfNeeded(ExtensionContext context) throws Exception {
        ContainerManager manager = getContainerManager(context);
        if (manager != null && !manager.isStarted()) {
            log.info("Starting containers in beforeAll for class: {}", context.getRequiredTestClass().getSimpleName());
            manager.startContainers();
        }
    }
    
    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        String testClassName = context.getRequiredTestClass().getName();
        
        // 클래스별 락 획득
        ReentrantLock lock = CLASS_LOCKS.get(testClassName);
        if (lock != null) {
            lock.lock();
            try {
                ContainerManager manager = getContainerManager(context);
                if (manager != null) {
                    log.info("Stopping containers for test class: {}", testClassName);
                    manager.stopContainers();
                    log.info("All containers stopped for test class: {}", testClassName);
                }
                
                // ThreadLocal 정리
                ContainerManagerHolder.clear();
            } finally {
                lock.unlock();
                // 락 정리
                CLASS_LOCKS.remove(testClassName);
            }
        }
    }
    
    private ContainerManager getOrCreateContainerManager(ExtensionContext context, 
                                                        EnableTestContainer annotation,
                                                        Class<?> testClass) {
        ExtensionContext.Store store = getStore(context);
        return store.getOrComputeIfAbsent(
            CONTAINER_MANAGER_KEY,
            key -> new ContainerManager(annotation, testClass),
            ContainerManager.class
        );
    }
    
    private ContainerManager getContainerManager(ExtensionContext context) {
        return getStore(context).get(CONTAINER_MANAGER_KEY, ContainerManager.class);
    }
    
    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }
}