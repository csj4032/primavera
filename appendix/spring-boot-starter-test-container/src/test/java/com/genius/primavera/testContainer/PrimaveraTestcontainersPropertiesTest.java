package com.genius.primavera.testContainer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Order(5)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Primavera TestContainers 프로퍼티 테스트")
class PrimaveraTestcontainersPropertiesTest {

    @Test
    @Order(1)
    @DisplayName("기본 MariaDB 설정 테스트")
    void shouldHaveDefaultMariaDBConfiguration() {
        PrimaveraTestcontainersProperties properties = new PrimaveraTestcontainersProperties();

        assertNotNull(properties.getMariadb());
        assertEquals("mariadb:11.4.7", properties.getMariadb().getImage());
        assertEquals("primavera", properties.getMariadb().getDatabaseName());
        assertEquals("primavera", properties.getMariadb().getUsername());
        assertEquals("primavera", properties.getMariadb().getPassword());
        assertNull(properties.getMariadb().getInitScript());

        log.info("Default MariaDB config: image={}, db={}, user={}",
                properties.getMariadb().getImage(),
                properties.getMariadb().getDatabaseName(),
                properties.getMariadb().getUsername());
    }

    @Test
    @Order(2)
    @DisplayName("기본 Redis 설정 테스트")
    void shouldHaveDefaultRedisConfiguration() {
        PrimaveraTestcontainersProperties properties = new PrimaveraTestcontainersProperties();

        assertNotNull(properties.getRedis());
        assertEquals("redis:6-alpine", properties.getRedis().getImage());
        assertEquals(6379, properties.getRedis().getPort());

        log.info("Default Redis config: image={}, port={}",
                properties.getRedis().getImage(),
                properties.getRedis().getPort());
    }

    @Test
    @Order(3)
    @DisplayName("기본 PostgreSQL 설정 테스트")
    void shouldHaveDefaultPostgreSQLConfiguration() {
        PrimaveraTestcontainersProperties properties = new PrimaveraTestcontainersProperties();

        assertNotNull(properties.getPostgreSQL());
        assertEquals("postgres:14", properties.getPostgreSQL().getImage());
        assertEquals("primavera", properties.getPostgreSQL().getDatabaseName());
        assertEquals("primavera", properties.getPostgreSQL().getUsername());
        assertEquals("primavera", properties.getPostgreSQL().getPassword());
        assertNull(properties.getPostgreSQL().getInitScript());
        assertNotNull(properties.getPostgreSQL().getStartupTimeout());

        log.info("Default PostgreSQL config: image={}, timeout={}",
                properties.getPostgreSQL().getImage(),
                properties.getPostgreSQL().getStartupTimeout());
    }

    @Test
    @Order(4)
    @DisplayName("기본 Kafka 설정 테스트")
    void shouldHaveDefaultKafkaConfiguration() {
        PrimaveraTestcontainersProperties properties = new PrimaveraTestcontainersProperties();

        assertNotNull(properties.getKafka());
        assertEquals("confluentinc/cp-kafka:latest", properties.getKafka().getImage());
        assertEquals("localhost:9092", properties.getKafka().getBootstrapServers());
        assertEquals("localhost:2181", properties.getKafka().getZookeeperConnect());
        assertEquals("localhost:9092", properties.getKafka().getAdvertisedListeners());
        assertEquals("PLAINTEXT:PLAINTEXT", properties.getKafka().getListenerSecurityProtocolMap());
        assertNotNull(properties.getKafka().getStartupTimeout());
        assertNotNull(properties.getKafka().getAdditionalProperties());

        log.info("Default Kafka config: image={}, bootstrap={}",
                properties.getKafka().getImage(),
                properties.getKafka().getBootstrapServers());
    }

    @Test
    @Order(5)
    @DisplayName("프로퍼티 바인딩을 통한 커스텀 설정 테스트")
    void shouldBindCustomPropertiesCorrectly() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("primavera.testcontainers.mariadb.image", "mariadb:11.4.7");
        properties.put("primavera.testcontainers.mariadb.database-name", "custom_db");
        properties.put("primavera.testcontainers.mariadb.username", "custom_user");
        properties.put("primavera.testcontainers.mariadb.password", "custom_pass");
        properties.put("primavera.testcontainers.mariadb.init-script", "sql/custom-init.sql");

        properties.put("primavera.testcontainers.redis.image", "redis:7-alpine");
        properties.put("primavera.testcontainers.redis.port", "6380");

        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source);

        PrimaveraTestcontainersProperties boundProperties = binder
                .bind("primavera.testcontainers", PrimaveraTestcontainersProperties.class)
                .get();

        // MariaDB 커스텀 설정 검증
        assertEquals("mariadb:11.4.7", boundProperties.getMariadb().getImage());
        assertEquals("custom_db", boundProperties.getMariadb().getDatabaseName());
        assertEquals("custom_user", boundProperties.getMariadb().getUsername());
        assertEquals("custom_pass", boundProperties.getMariadb().getPassword());
        assertEquals("sql/custom-init.sql", boundProperties.getMariadb().getInitScript());

        // Redis 커스텀 설정 검증
        assertEquals("redis:7-alpine", boundProperties.getRedis().getImage());
        assertEquals(6380, boundProperties.getRedis().getPort());

        log.info("Custom bound properties - MariaDB: {}, Redis port: {}",
                boundProperties.getMariadb().getImage(),
                boundProperties.getRedis().getPort());
    }

    @Test
    @Order(6)
    @DisplayName("MariaDB 설정 개별 수정 테스트")
    void shouldAllowMariaDBConfigurationModification() {
        PrimaveraTestcontainersProperties.Mariadb mariadbConfig =
                new PrimaveraTestcontainersProperties.Mariadb();

        mariadbConfig.setImage("mariadb:11.4.7");
        mariadbConfig.setDatabaseName("test_database");
        mariadbConfig.setUsername("test_user");
        mariadbConfig.setPassword("secure_password");
        mariadbConfig.setInitScript("sql/test-schema.sql");

        assertEquals("mariadb:11.4.7", mariadbConfig.getImage());
        assertEquals("test_database", mariadbConfig.getDatabaseName());
        assertEquals("test_user", mariadbConfig.getUsername());
        assertEquals("secure_password", mariadbConfig.getPassword());
        assertEquals("sql/test-schema.sql", mariadbConfig.getInitScript());

        log.info("Modified MariaDB config: {}/{} with script: {}",
                mariadbConfig.getDatabaseName(),
                mariadbConfig.getUsername(),
                mariadbConfig.getInitScript());
    }
}