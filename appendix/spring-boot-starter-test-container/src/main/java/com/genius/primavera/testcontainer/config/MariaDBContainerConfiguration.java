package com.genius.primavera.testcontainer.config;

import com.genius.primavera.testcontainer.ContainerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.context.annotation.Scope;
import org.testcontainers.containers.MariaDBContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MariaDB TestContainer 설정 - 다중 컨테이너 지원
 * ContainerSpec에 정의된 각 MariaDB 컨테이너를 개별적으로 생성
 * name 기반으로 Bean 이름과 Qualifier 결정
 */
@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class MariaDBContainerConfiguration {

    private static final ConcurrentHashMap<String, MariaDBContainer<?>> containerCache = new ConcurrentHashMap<>();
    private static final AtomicInteger containerCounter = new AtomicInteger(0);

    /**
     * 시스템 프로퍼티에서 ContainerSpec 정보를 읽어서 MariaDB 컨테이너들을 동적으로 생성
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public MariaDBContainerRegistrar mariaDBContainerRegistrar() {
        return new MariaDBContainerRegistrar();
    }

    /**
     * MariaDB ContainerSpec들을 파싱하고 각각에 대해 컨테이너 Bean을 등록하는 클래스
     */
    public static class MariaDBContainerRegistrar {
        
        public MariaDBContainerRegistrar() {
            registerMariaDBContainers();
        }
        
        private void registerMariaDBContainers() {
            int specCount = Integer.parseInt(System.getProperty("primavera.testcontainer.spec.count", "0"));
            log.info("MariaDB 컨테이너 등록 시작 - 전체 ContainerSpec 개수: {}", specCount);
            
            List<ContainerSpec> mariadbSpecs = new ArrayList<>();
            
            // 시스템 프로퍼티에서 MariaDB 스펙들만 필터링
            for (int i = 0; i < specCount; i++) {
                String prefix = "primavera.testcontainer.spec." + i;
                String typeStr = System.getProperty(prefix + ".type");
                
                if ("MARIADB".equals(typeStr)) {
                    ContainerSpec spec = new ContainerSpec(
                        i,
                        System.getProperty(prefix + ".name"),
                        System.getProperty(prefix + ".initScript"),
                        Boolean.parseBoolean(System.getProperty(prefix + ".reuse")),
                        Integer.parseInt(System.getProperty(prefix + ".port")),
                        System.getProperty(prefix + ".databaseName"),
                        System.getProperty(prefix + ".username"),
                        System.getProperty(prefix + ".password"),
                        System.getProperty(prefix + ".labels", "").split(",")
                    );
                    mariadbSpecs.add(spec);
                }
            }
            
            log.info("발견된 MariaDB ContainerSpec 개수: {}", mariadbSpecs.size());
            
            // 각 MariaDB 스펙에 대해 컨테이너 생성
            for (ContainerSpec spec : mariadbSpecs) {
                createMariaDBContainer(spec);
            }
        }
        
        private void createMariaDBContainer(ContainerSpec spec) {
            String containerName = spec.name + "MariaDBContainer";
            int containerNumber = containerCounter.incrementAndGet();
            
            log.info("★ MariaDB TestContainer 생성: {}", containerName);
            log.info("   - 이름: {}", spec.name);
            log.info("   - 초기화 스크립트: {}", spec.initScript);
            log.info("   - 재사용: {}", spec.reuse);
            log.info("   - 데이터베이스명: {}", spec.databaseName);
            
            // 컨테이너 생성
            MariaDBContainer<?> container = new MariaDBContainer<>(ContainerType.MARIADB.getDefaultImage())
                    .withUsername(spec.username)
                    .withPassword(spec.password)
                    .withDatabaseName(spec.databaseName)
                    .withInitScript(spec.initScript)
                    .withReuse(spec.reuse)
                    .withLabel("container-name", spec.name)
                    .withLabel("container-number", String.valueOf(containerNumber))
                    .withLabel("creation-time", String.valueOf(System.currentTimeMillis()));
            
            // 추가 라벨 적용
            for (String label : spec.labels) {
                if (!label.isEmpty()) {
                    String[] keyValue = label.split("=", 2);
                    if (keyValue.length == 2) {
                        container = container.withLabel(keyValue[0], keyValue[1]);
                    }
                }
            }
            
            // 포트 오버라이드 (필요한 경우)
            if (spec.port > 0) {
                container = container.withExposedPorts(spec.port);
            }
            
            // 캐시에 저장
            containerCache.put(spec.name, container);
            
            log.info("★ MariaDB 컨테이너 생성 완료: {}", containerName);
            log.info("   - JVM Identity: {}", System.identityHashCode(container));
            log.info("   - 캐시 키: {}", spec.name);
        }
    }
    
    /**
     * ContainerSpec 정보를 담는 내부 클래스
     */
    private static class ContainerSpec {
        final int index;
        final String name;
        final String initScript;
        final boolean reuse;
        final int port;
        final String databaseName;
        final String username;
        final String password;
        final String[] labels;
        
        ContainerSpec(int index, String name, String initScript, boolean reuse, int port, 
                     String databaseName, String username, String password, String[] labels) {
            this.index = index;
            this.name = name;
            this.initScript = initScript;
            this.reuse = reuse;
            this.port = port;
            this.databaseName = databaseName;
            this.username = username;
            this.password = password;
            this.labels = labels;
        }
    }

    /**
     * 각 컨테이너 이름별로 개별 Bean 생성
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public MariaDBContainerBeanRegistry mariaDBContainerBeanRegistry() {
        return new MariaDBContainerBeanRegistry();
    }
    
    /**
     * 동적으로 각 MariaDB 컨테이너 Bean을 등록하는 클래스
     */
    public static class MariaDBContainerBeanRegistry {
        
        public MariaDBContainerBeanRegistry() {
            registerContainerBeans();
        }
        
        private void registerContainerBeans() {
            log.info("MariaDB 컨테이너 Bean 등록 시작");
            
            // 캐시된 모든 컨테이너에 대해 Bean 등록 정보 로깅
            for (String containerName : containerCache.keySet()) {
                MariaDBContainer<?> container = containerCache.get(containerName);
                log.info("등록된 MariaDB 컨테이너: {} -> {}", containerName, System.identityHashCode(container));
                
                // 컨테이너 시작
                if (!container.isRunning()) {
                    log.info("컨테이너 시작: {}", containerName);
                    container.start();
                }
            }
        }
    }

    /**
     * 캐시된 컨테이너 정보 조회
     */
    public static int getCachedContainerCount() {
        return containerCache.size();
    }

    /**
     * 특정 이름의 컨테이너 조회
     */
    public static MariaDBContainer<?> getContainer(String name) {
        return containerCache.get(name);
    }

    /**
     * 전체 캐시 정리
     */
    public static void clearCache() {
        log.info("MariaDB 컨테이너 캐시 정리: {} 개 컨테이너", containerCache.size());
        
        containerCache.values().forEach(container -> {
            try {
                if (container.isRunning()) {
                    log.info("컨테이너 중지: {}", container.getContainerId());
                    container.stop();
                }
            } catch (Exception e) {
                log.warn("컨테이너 중지 중 오류: {}", e.getMessage());
            }
        });
        
        containerCache.clear();
        containerCounter.set(0);
        
        log.info("MariaDB 컨테이너 캐시 정리 완료");
    }
}