package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.ConfigurationLoader;
import com.genius.primavera.testcontainers.factory.SpecFactory;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 컨테이너 생명주기 관리에 집중하는 슬림화된 매니저 클래스
 * 설정 로딩과 Spec 생성은 별도 클래스에 위임
 */
@Slf4j
public class ContainerLifecycleManager {

    private final EnableTestContainers annotation;
    private final Class<?> testClass;
    private final Map<String, ContainerInfo> containers = new ConcurrentHashMap<>();
    private final ContainerConfiguration configuration;
    private volatile boolean started = false;

    public ContainerLifecycleManager(EnableTestContainers annotation, Class<?> testClass) {
        this.annotation = annotation;
        this.testClass = testClass;
        this.configuration = new ConfigurationLoader().loadConfiguration();
    }

    /**
     * 모든 컨테이너를 병렬로 시작
     */
    public synchronized void startContainers() {
        if (started) {
            return;
        }

        log.info("Starting {} containers for test class: {}",
                annotation.value().length, testClass.getSimpleName());

        List<CompletableFuture<Void>> futures = Arrays.stream(annotation.value())
                .map(this::startContainerAsync)
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(120, TimeUnit.SECONDS);
            started = true;
            log.info("All containers started successfully for test class: {}", testClass.getSimpleName());
        } catch (Exception e) {
            log.error("Failed to start containers for test class: {}", testClass.getSimpleName(), e);
            throw new RuntimeException("Container startup failed", e);
        }
    }

    /**
     * 모든 컨테이너를 병렬로 중지
     */
    public void stopContainers() {
        if (!started) {
            return;
        }

        log.info("Stopping {} containers for test class: {}",
                containers.size(), testClass.getSimpleName());

        containers.values().parallelStream().forEach(containerInfo -> {
            try {
                containerInfo.container().stop();
                log.debug("Stopped container: {}", containerInfo.name());
            } catch (Exception e) {
                log.error("Failed to stop container: {}", containerInfo.name(), e);
            }
        });

        containers.clear();
        started = false;
        log.info("All containers stopped for test class: {}", testClass.getSimpleName());
    }

    public boolean isStarted() {
        return started;
    }

    public Collection<ContainerInfo> getAllContainers() {
        return Collections.unmodifiableCollection(containers.values());
    }

    public ContainerInfo getContainer(String name) {
        return containers.get(name);
    }

    /**
     * 단일 컨테이너를 비동기적으로 시작
     */
    private CompletableFuture<Void> startContainerAsync(EnableTestContainers.TestContainer containerDef) {
        return CompletableFuture.runAsync(() -> {
            String name = containerDef.name();
            ContainerType type = containerDef.type();

            try {
                log.info("Starting {} container: {}", type, name);

                BaseContainerSpec spec = resolveContainerSpec(name, type);
                GenericContainer<?> container = ContainerFactory.create(type, spec);
                container.start();

                ContainerInfo info = new ContainerInfo(name, type, container, spec);
                containers.put(name, info);

                log.info("Started {} container '{}' on {}:{}",
                        type, name, container.getHost(), container.getFirstMappedPort());

            } catch (Exception e) {
                log.error("Failed to start container: {}", name, e);
                throw new RuntimeException("Failed to start container: " + name, e);
            }
        });
    }

    /**
     * 컨테이너 스펙 해결: 설정 > 기본값 순서로 적용
     */
    private BaseContainerSpec resolveContainerSpec(String name, ContainerType type) {
        Optional<ContainerConfiguration.ContainerInstanceConfig> configOpt = 
            configuration.getContainerConfig(name);

        if (configOpt.isPresent()) {
            ContainerConfiguration.ContainerInstanceConfig instanceConfig = configOpt.get();
            
            // 타입 불일치 검증
            if (instanceConfig.getType() != type) {
                log.warn("Type mismatch for container '{}': annotation={}, config={}. Using annotation type.", 
                    name, type, instanceConfig.getType());
                instanceConfig.setType(type);
            }
            
            BaseContainerSpec spec = instanceConfig.getSpecForType();
            if (spec != null) {
                log.debug("Using configured spec for container '{}': {}", name, spec.getClass().getSimpleName());
                return spec;
            }
        }

        // 설정이 없는 경우 기본 Spec 생성
        log.debug("Creating default spec for container '{}'", name);
        return SpecFactory.createDefaultSpec(type);
    }
}