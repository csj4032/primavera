package com.genius.primavera.testcontainer;

import com.genius.primavera.testcontainer.strategy.ContainerStrategy;
import com.genius.primavera.testcontainer.strategy.ContainerStrategyFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.GenericContainer;

public class PrimaveraTestcontainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        PrimaveraTestcontainersProperties properties = Binder.get(environment).bind("primavera.testcontainers", PrimaveraTestcontainersProperties.class).orElse(new PrimaveraTestcontainersProperties());
        properties.getContainers().forEach((containerType, config) -> {
            if (config.isEnabled()) {
                initializeContainer(applicationContext, containerType, config, properties.getLifecycleMode());
            }
        });
    }
    
    private void initializeContainer(ConfigurableApplicationContext applicationContext, 
                                   String containerType, 
                                   PrimaveraTestcontainersProperties.ContainerConfig config,
                                   ContainerLifecycleMode lifecycleMode) {
        ContainerStrategy strategy = ContainerStrategyFactory.getStrategy(containerType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported container type: " + containerType);
        }
        
        if (!ContainerManager.containsContainer(containerType, lifecycleMode)) {
            try {
                GenericContainer<?> container = strategy.createContainer(config);
                container.start();
                
                ContainerManager.putContainer(containerType, container, lifecycleMode);
                strategy.configureApplicationContext(applicationContext, container);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize container: " + containerType, e);
            }
        } else {
            // 기존 컨테이너가 있다면 설정만 다시 적용
            GenericContainer<?> existingContainer = ContainerManager.getContainer(containerType, lifecycleMode);
            strategy.configureApplicationContext(applicationContext, existingContainer);
        }
    }
    
    
    public static void stopContainers(ContainerLifecycleMode mode) {
        ContainerManager.stopContainers(mode);
    }
    
    public static GenericContainer<?> getContainer(String containerType, ContainerLifecycleMode mode) {
        return ContainerManager.getContainer(containerType, mode);
    }
    
    // 하위 호환성을 위한 메서드 (기본값: REUSE)
    public static GenericContainer<?> getContainer(String containerType) {
        return ContainerManager.getContainer(containerType, ContainerLifecycleMode.REUSE);
    }
    
    public static void stopContainers() {
        ContainerManager.stopAllContainers();
    }
}