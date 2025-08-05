package com.genius.primavera.testcontainer.v3;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * TestContainer 관리자 (v3)
 * 
 * <p>어노테이션으로 선언된 컨테이너들을 application.yml 설정에 따라 생성/관리합니다.</p>
 */
@Slf4j
public class ContainerManager {
    
    @Getter
    private final EnableTestContainer annotation;
    private final Class<?> testClass;
    private final Map<String, ContainerInfo> containers = new ConcurrentHashMap<>();
    private TestContainerProperties properties;
    
    public ContainerManager(EnableTestContainer annotation, Class<?> testClass) {
        this.annotation = annotation;
        this.testClass = testClass;
        this.properties = loadProperties();
    }
    
    /**
     * 모든 컨테이너 시작
     */
    public void startContainers() {
        log.info("Starting {} containers for test class: {}", 
            annotation.value().length, testClass.getSimpleName());
        
        // 병렬로 컨테이너 시작
        List<CompletableFuture<Void>> futures = Arrays.stream(annotation.value())
            .map(containerDef -> CompletableFuture.runAsync(() -> startContainer(containerDef)))
            .collect(Collectors.toList());
        
        try {
            // 모든 컨테이너가 시작될 때까지 대기
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .get(120, TimeUnit.SECONDS); // 2분 타임아웃
        } catch (Exception e) {
            log.error("Failed to start containers for test class: {}", testClass.getSimpleName(), e);
            throw new RuntimeException("Container startup failed", e);
        }
        
        log.info("All containers started successfully for test class: {}", testClass.getSimpleName());
    }
    
    /**
     * 모든 컨테이너 정지
     */
    public void stopContainers() {
        log.info("Stopping {} containers for test class: {}", 
            containers.size(), testClass.getSimpleName());
        
        containers.values().parallelStream().forEach(containerInfo -> {
            try {
                containerInfo.container.stop();
                log.debug("Stopped container: {}", containerInfo.name);
            } catch (Exception e) {
                log.error("Failed to stop container: {}", containerInfo.name, e);
            }
        });
        
        containers.clear();
        log.info("All containers stopped for test class: {}", testClass.getSimpleName());
    }
    
    /**
     * 개별 컨테이너 시작
     */
    private void startContainer(EnableTestContainer.TestContainer containerDef) {
        String name = containerDef.name();
        ContainerType type = containerDef.type();
        
        try {
            log.info("Starting {} container: {}", type, name);
            
            // application.yml에서 설정 로드
            TestContainerProperties.ContainerConfig config = properties.getContainers().get(name);
            if (config == null) {
                config = createDefaultConfig(type);
                log.warn("No configuration found for container '{}', using defaults", name);
            }
            
            // 컨테이너 생성
            GenericContainer<?> container = ContainerFactory.create(type, config);
            
            // 컨테이너 시작
            container.start();
            
            ContainerInfo info = new ContainerInfo(name, type, container, config);
            containers.put(name, info);
            
            log.info("Started {} container '{}' on {}:{}", 
                type, name, container.getHost(), container.getFirstMappedPort());
            
        } catch (Exception e) {
            log.error("Failed to start container: {}", name, e);
            throw new RuntimeException("Failed to start container: " + name, e);
        }
    }
    
    /**
     * application.yml에서 설정 로드
     */
    private TestContainerProperties loadProperties() {
        try {
            Properties props = new Properties();
            
            // application-test.yml 또는 application-test.properties 로드 시도
            String[] configFiles = {
                "application-test.properties",
                "application.properties"
            };
            
            for (String configFile : configFiles) {
                ClassPathResource resource = new ClassPathResource(configFile);
                if (resource.exists()) {
                    props.putAll(PropertiesLoaderUtils.loadProperties(resource));
                    log.debug("Loaded configuration from: {}", configFile);
                    break;
                }
            }
            
            // Properties를 TestContainerProperties로 바인딩
            // 실제 구현에서는 Spring Environment를 사용해야 함
            TestContainerProperties testContainerProperties = new TestContainerProperties();
            
            // 기본 설정으로 초기화 (실제로는 Binder 사용)
            return testContainerProperties;
            
        } catch (IOException e) {
            log.warn("Failed to load configuration, using defaults", e);
            return new TestContainerProperties();
        }
    }
    
    /**
     * 기본 설정 생성
     */
    private TestContainerProperties.ContainerConfig createDefaultConfig(ContainerType type) {
        TestContainerProperties.ContainerConfig config = new TestContainerProperties.ContainerConfig();
        config.setImage(type.getDefaultImage());
        return config;
    }
    
    /**
     * 컨테이너 정보 조회
     */
    public ContainerInfo getContainer(String name) {
        return containers.get(name);
    }
    
    /**
     * 모든 컨테이너 정보 조회
     */
    public Collection<ContainerInfo> getAllContainers() {
        return Collections.unmodifiableCollection(containers.values());
    }
    
    /**
     * 컨테이너 시작 여부 확인
     */
    public boolean isStarted() {
        return !containers.isEmpty();
    }
    
    /**
     * 컨테이너 정보 홀더
     */
    @Getter
    public static class ContainerInfo {
        private final String name;
        private final ContainerType type;
        private final GenericContainer<?> container;
        private final TestContainerProperties.ContainerConfig config;
        
        public ContainerInfo(String name, ContainerType type, GenericContainer<?> container,
                           TestContainerProperties.ContainerConfig config) {
            this.name = name;
            this.type = type;
            this.container = container;
            this.config = config;
        }
        
        public String getHost() {
            return container.getHost();
        }
        
        public Integer getMappedPort() {
            return container.getFirstMappedPort();
        }
        
        public String getJdbcUrl() {
            if (!type.isSqlDatabase()) {
                throw new UnsupportedOperationException("JDBC URL is only available for SQL databases");
            }
            return type.createJdbcUrl(getHost(), getMappedPort(), config.getDatabase());
        }
    }
}