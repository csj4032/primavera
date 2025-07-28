package com.genius.primavera.test;

import com.genius.primavera.test.factory.ContainerStrategyFactory;
import com.genius.primavera.test.strategy.ContainerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ApplicationContextInitializer를 통한 통합 TestContainers 관리
 * <p>
 * Spring 컨텍스트 초기화 시점에 Strategy Pattern을 사용하여 컨테이너를 시작하고 프로퍼티를 설정합니다.
 *
 * @EnablePrimaveraTestcontainers 애노테이션을 통해 필요한 컨테이너 타입을 지정할 수 있습니다.
 */
@Slf4j
public class PrimaveraTestcontainersContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Map<String, ContainerStrategy> strategyCache = new ConcurrentHashMap<>();
    private static ContainerStrategyFactory factory;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("Initializing Primavera Testcontainers with Strategy Pattern...");
        
        initializeFactory(applicationContext);
        EnablePrimaveraTestcontainers annotation = findTestcontainersAnnotation(applicationContext);

        if (annotation == null) {
            log.info("EnablePrimaveraTestcontainers annotation not found. Starting default MariaDB container.");
            startContainer(ContainerType.MARIADB, applicationContext);
            return;
        }

        ContainerType[] containerTypes = annotation.value();
        for (ContainerType containerType : containerTypes) {
            startContainer(containerType, applicationContext);
        }
    }
    
    private void initializeFactory(ConfigurableApplicationContext applicationContext) {
        if (factory == null) {
            factory = new ContainerStrategyFactory(
                new com.genius.primavera.test.config.MariaDBContainerConfig(),
                new com.genius.primavera.test.config.RedisContainerConfig(),
                new com.genius.primavera.test.config.KafkaContainerConfig(),
                new com.genius.primavera.test.config.PostgreSQLContainerConfig()
            );
        }
    }
    
    private void startContainer(ContainerType containerType, ConfigurableApplicationContext applicationContext) {
        ContainerStrategy strategy = strategyCache.computeIfAbsent(
            containerType.name(), 
            k -> factory.getStrategy(containerType)
        );
        
        if (!strategy.isRunning()) {
            log.info("Starting {} container...", containerType.name());
            strategy.startContainer(applicationContext);
            log.info("{} container started successfully", containerType.name());
        }
    }

    private EnablePrimaveraTestcontainers findTestcontainersAnnotation(ConfigurableApplicationContext context) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                EnablePrimaveraTestcontainers annotation = clazz.getAnnotation(EnablePrimaveraTestcontainers.class);
                if (annotation != null) return annotation;
                try {
                    var method = clazz.getDeclaredMethod(element.getMethodName());
                    annotation = method.getAnnotation(EnablePrimaveraTestcontainers.class);
                    if (annotation != null) {
                        return annotation;
                    }
                } catch (NoSuchMethodException ignored) {
                    // 메서드가 없으면 무시
                }

            } catch (ClassNotFoundException ignored) {
                // 클래스를 찾을 수 없으면 무시
            }
        }

        return null;
    }


    /**
     * 컨테이너 정보 접근을 위한 헬퍼 메서드들
     */
    public static GenericContainer<?> getContainer(ContainerType containerType) {
        ContainerStrategy strategy = strategyCache.get(containerType.name());
        return strategy != null ? strategy.getContainer() : null;
    }

    /**
     * 모든 컨테이너 정지 (테스트 종료 시 호출)
     */
    public static void stopAllContainers() {
        strategyCache.values().forEach(strategy -> {
            GenericContainer<?> container = strategy.getContainer();
            if (container != null && container.isRunning()) {
                container.stop();
            }
        });
        strategyCache.clear();
    }
}