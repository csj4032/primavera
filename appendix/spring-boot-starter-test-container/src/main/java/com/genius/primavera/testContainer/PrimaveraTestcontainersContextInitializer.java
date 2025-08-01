package com.genius.primavera.testContainer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.primavera.testContainer.factory.ContainerStrategyFactory;
import com.genius.primavera.testContainer.strategy.ContainerStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.GenericContainer;

import java.util.Arrays;
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
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("Initializing Primavera Testcontainers with Strategy Pattern...");

        // Spring 컨텍스트에서 ContainerStrategyFactory 빈을 가져옴 <--- 변경
        // 이 시점에는 이미 자동 설정으로 인해 ContainerStrategyFactory 빈이 생성되어 있어야 합니다.
        if (factory == null) {
            initializeFactory(applicationContext.getEnvironment());
            log.info("ContainerStrategyFactory bean obtained from application context.");
        }

        String containerTypesJson = System.getProperty(PrimaveraTestcontainersListener.TESTCONTAINERS_CONFIG_PROPERTY);

        ContainerType[] containerTypes;
        if (containerTypesJson != null && !containerTypesJson.isEmpty()) {
            try {
                containerTypes = objectMapper.readValue(containerTypesJson, ContainerType[].class);
                log.info("Loaded container types from system property: {}", Arrays.toString(containerTypes));
            } catch (Exception e) {
                log.error("Failed to parse container types from system property, defaulting to MariaDB.", e);
                containerTypes = new ContainerType[]{ContainerType.MARIADB};
            }
        } else {
            log.info("EnablePrimaveraTestcontainers annotation not signaled. No Testcontainers will be started by this initializer.");
            return;
        }

        for (ContainerType containerType : containerTypes) {
            startContainer(containerType, applicationContext);
        }
    }

    private void initializeFactory(Environment environment) {
        if (factory == null) {
            factory = new ContainerStrategyFactory(environment);
        }
    }

    private void startContainer(ContainerType containerType, ConfigurableApplicationContext applicationContext) {
        log.info("Attempting to start {} container", containerType.name());
        try {
            ContainerStrategy strategy = strategyCache.computeIfAbsent(containerType.name(), k -> {
                log.info("Creating strategy for {} container", containerType.name());
                return factory.getStrategy(containerType);
            });
            
            if (!strategy.isRunning()) {
                log.info("Starting {} container...", containerType.name());
                strategy.startContainer(applicationContext);
                log.info("{} container started successfully", containerType.name());
            } else {
                log.info("{} container is already running", containerType.name());
            }
        } catch (Exception e) {
            log.error("Failed to start {} container: {}", containerType.name(), e.getMessage(), e);
            // Remove failed strategy from cache to avoid confusion
            strategyCache.remove(containerType.name());
            throw new RuntimeException("Failed to start " + containerType.name() + " container", e);
        }
    }

    public static GenericContainer<?> getContainer(ContainerType containerType) {
        log.debug("Getting container for type: {}", containerType.name());
        ContainerStrategy strategy = strategyCache.get(containerType.name());
        if (strategy == null) {
            log.warn("No strategy found for container type: {}. Available strategies: {}", 
                    containerType.name(), strategyCache.keySet());
            return null;
        }
        GenericContainer<?> container = strategy.getContainer();
        if (container == null) {
            log.warn("Strategy returned null container for type: {}", containerType.name());
        }
        return container;
    }

    public static void stopAllContainers() {
        log.info("Stopping all test containers...");
        strategyCache.values().forEach(strategy -> {
            GenericContainer<?> container = strategy.getContainer();
            if (container != null && container.isRunning()) {
                container.stop();
                log.info("Stopped {} container.", strategy.getContainerType());
            }
        });
        strategyCache.clear();
        log.info("All test containers stopped and cache cleared.");
    }
}