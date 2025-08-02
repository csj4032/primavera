package com.genius.primavera.testcontainer;

import com.genius.primavera.testcontainer.strategy.ContainerStrategy;
import com.genius.primavera.testcontainer.strategy.ContainerStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableConfigurationProperties(PrimaveraTestcontainersProperties.class)
public class PrimaveraTestcontainersConfiguration {
    
    @Autowired
    private PrimaveraTestcontainersProperties properties;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @PostConstruct
    public void initializeContainers() {
        System.out.println("🚀 PrimaveraTestcontainersConfiguration.initializeContainers() 호출됨!");
        
        String testClassName = getCurrentTestClassName();
        
        System.out.println("📋 컨테이너 설정 개수: " + properties.getContainers().size());
        System.out.println("🔄 라이프사이클 모드: " + properties.getLifecycleMode());
        System.out.println("📝 테스트 클래스: " + testClassName);
        
        properties.getContainers().forEach((containerType, config) -> {
            System.out.println("🔍 컨테이너 처리 중: " + containerType + ", 활성화: " + config.isEnabled());
            if (config.isEnabled()) {
                initializeContainer((ConfigurableApplicationContext) applicationContext, containerType, config, properties.getLifecycleMode(), testClassName);
            }
        });
        
        System.out.println("✅ PrimaveraTestcontainersConfiguration.initializeContainers() 완료!");
    }
    
    private void initializeContainer(ConfigurableApplicationContext applicationContext, 
                                   String containerType, 
                                   PrimaveraTestcontainersProperties.ContainerConfig config,
                                   ContainerLifecycleMode lifecycleMode,
                                   String testClassName) {
        ContainerStrategy strategy = ContainerStrategyFactory.getStrategy(containerType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported container type: " + containerType);
        }
        
        if (!ContainerManager.containsContainer(containerType, lifecycleMode, testClassName)) {
            try {
                GenericContainer<?> container = strategy.createContainer(config);
                container.start();
                
                ContainerManager.putContainer(containerType, container, lifecycleMode, testClassName);
                strategy.configureApplicationContext(applicationContext, container);
                
                System.out.println("🚀 새 컨테이너 생성: " + testClassName + " -> " + containerType + " (ID: " + container.getContainerId() + ")");
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize container: " + containerType, e);
            }
        } else {
            // 기존 컨테이너가 있다면 설정만 다시 적용
            GenericContainer<?> existingContainer = ContainerManager.getContainer(containerType, lifecycleMode, testClassName);
            strategy.configureApplicationContext(applicationContext, existingContainer);
            
            System.out.println("♻️ 기존 컨테이너 재사용: " + testClassName + " -> " + containerType + " (ID: " + existingContainer.getContainerId() + ")");
        }
    }
    
    private String getCurrentTestClassName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains("Test") && !className.contains("Spring") && !className.contains("junit") && !className.contains("PrimaveraTestcontainers")) {
                return className;
            }
        }
        return "UnknownTestClass";
    }
}