package com.genius.primavera.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ApplicationContextInitializer를 통한 통합 TestContainers 관리
 * <p>
 * Spring 컨텍스트 초기화 시점에 모든 컨테이너를 시작하고 프로퍼티를 설정합니다.
 *
 * @EnablePrimaveraTestcontainers 애노테이션을 통해 필요한 컨테이너 타입을 지정할 수 있습니다.
 */
@Slf4j
public class PrimaveraTestcontainersContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Map<String, GenericContainer<?>> containerCache = new ConcurrentHashMap<>();

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("Initializing Primavera Testcontainers...");
        EnablePrimaveraTestcontainers annotation = findTestcontainersAnnotation(applicationContext);

        if (annotation == null) {
            log.info("EnablePrimaveraTestcontainers annotation not found. Starting default MariaDB container.");
            startMariaDBContainer(applicationContext);
            return;
        }

        ContainerType[] containerTypes = annotation.value();

        for (ContainerType containerType : containerTypes) {
            switch (containerType) {
                case MARIADB -> startMariaDBContainer(applicationContext);
                case REDIS -> startRedisContainer(applicationContext);
                case KAFKA -> startKafkaContainer(applicationContext);
                case POSTGRESQL -> startPostgreSQLContainer(applicationContext);
            }
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

    private void startMariaDBContainer(ConfigurableApplicationContext context) {
        MariaDBContainer<?> mariadb = (MariaDBContainer<?>) containerCache.computeIfAbsent("mariadb", k ->
                new MariaDBContainer<>(DockerImageName.parse("mariadb:11.4.7"))
                        .withDatabaseName("primavera")
                        .withUsername("primavera")
                        .withPassword("primavera")
        );

        if (!mariadb.isRunning()) {
            mariadb.start();
        }

        // Spring Environment에 데이터소스 프로퍼티 설정
        ConfigurableEnvironment environment = context.getEnvironment();
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", mariadb.getJdbcUrl());
        properties.put("spring.datasource.username", mariadb.getUsername());
        properties.put("spring.datasource.password", mariadb.getPassword());
        properties.put("spring.datasource.driver-class-name", "org.mariadb.jdbc.Driver");

        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-mariadb", properties));
    }

    private void startRedisContainer(ConfigurableApplicationContext context) {
        GenericContainer<?> redis = containerCache.computeIfAbsent("redis", k ->
                new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                        .withExposedPorts(6379)
        );

        if (!redis.isRunning()) {
            redis.start();
        }

        ConfigurableEnvironment environment = context.getEnvironment();
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.data.redis.host", redis.getHost());
        properties.put("spring.data.redis.port", redis.getMappedPort(6379));

        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-redis", properties));
    }

    private void startKafkaContainer(ConfigurableApplicationContext context) {
        ConfluentKafkaContainer kafka = (ConfluentKafkaContainer) containerCache.computeIfAbsent("kafka", k ->
                new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
        );

        if (!kafka.isRunning()) {
            kafka.start();
        }

        ConfigurableEnvironment environment = context.getEnvironment();
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());

        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-kafka", properties));
    }

    private void startPostgreSQLContainer(ConfigurableApplicationContext context) {
        PostgreSQLContainer<?> postgresql = (PostgreSQLContainer<?>) containerCache.computeIfAbsent("postgresql", k ->
                new PostgreSQLContainer<>("postgres:15-alpine")
                        .withDatabaseName("testdb")
                        .withUsername("test")
                        .withPassword("test")
        );

        if (!postgresql.isRunning()) {
            postgresql.start();
        }

        ConfigurableEnvironment environment = context.getEnvironment();
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", postgresql.getJdbcUrl());
        properties.put("spring.datasource.username", postgresql.getUsername());
        properties.put("spring.datasource.password", postgresql.getPassword());
        properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");

        environment.getPropertySources().addFirst(new MapPropertySource("testcontainers-postgresql", properties));
    }

    /**
     * 컨테이너 정보 접근을 위한 헬퍼 메서드들
     */
    public static GenericContainer<?> getContainer(String name) {
        return containerCache.get(name);
    }

    public static MariaDBContainer<?> getMariaDBContainer() {
        return (MariaDBContainer<?>) containerCache.get("mariadb");
    }

    public static GenericContainer<?> getRedisContainer() {
        return containerCache.get("redis");
    }

    public static ConfluentKafkaContainer getKafkaContainer() {
        return (ConfluentKafkaContainer) containerCache.get("kafka");
    }

    public static PostgreSQLContainer<?> getPostgreSQLContainer() {
        return (PostgreSQLContainer<?>) containerCache.get("postgresql");
    }

    /**
     * 모든 컨테이너 정지 (테스트 종료 시 호출)
     */
    public static void stopAllContainers() {
        containerCache.values().forEach(container -> {
            if (container.isRunning()) {
                container.stop();
            }
        });
        containerCache.clear();
    }
}