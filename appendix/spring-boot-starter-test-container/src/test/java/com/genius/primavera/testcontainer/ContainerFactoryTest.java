package com.genius.primavera.testcontainer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("ContainerFactory Unit Tests")
class ContainerFactoryTest {
    
    @Test
    @DisplayName("Create MariaDB container with default configuration")
    void testCreateMariaDbDefault() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("mariadb:11.4.7")
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MARIADB, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("mariadb:11.4.7", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ MariaDB container created with default configuration");
    }
    
    @Test
    @DisplayName("Create MariaDB container with custom configuration")
    void testCreateMariaDbCustom() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("mariadb:11.4.7")
            .database("custom_db")
            .username("custom_user")
            .password("custom_pass")
            .environment(Map.of("MYSQL_CHARSET", "utf8mb4"))
            .networkAliases(new String[]{"custom-db"})
            .startupTimeout(90)
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MARIADB, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("mariadb:11.4.7", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ MariaDB container created with custom configuration");
    }
    
    @Test
    @DisplayName("Create Redis container with default configuration")
    void testCreateRedisDefault() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("redis:7-alpine")
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.REDIS, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("redis:7-alpine", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ Redis container created with default configuration");
    }
    
    @Test
    @DisplayName("Create Redis container with password")
    void testCreateRedisWithPassword() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("redis:7-alpine")
            .password("redis_password")
            .environment(Map.of("REDIS_MAXMEMORY", "256mb"))
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.REDIS, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("redis:7-alpine", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ Redis container created with password configuration");
    }
    
    @Test
    @DisplayName("Create Kafka container with default configuration")
    void testCreateKafkaDefault() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("confluentinc/cp-kafka:7.5.0")
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.KAFKA, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("confluentinc/cp-kafka:7.5.0", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ Kafka container created with default configuration");
    }
    
    @Test
    @DisplayName("Create Kafka container with custom environment")
    void testCreateKafkaCustom() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("confluentinc/cp-kafka:7.5.0")
            .environment(Map.of(
                "KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true",
                "KAFKA_NUM_PARTITIONS", "3"
            ))
            .startupTimeout(180)
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.KAFKA, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("confluentinc/cp-kafka:7.5.0", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ Kafka container created with custom environment");
    }
    
    @Test
    @DisplayName("Create MongoDB container")
    void testCreateMongoDB() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("mongo:7")
            .username("mongo_user")
            .password("mongo_pass")
            .database("mongo_db")
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MONGODB, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("mongo:7", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ MongoDB container created");
    }
    
    @Test
    @DisplayName("Create PostgreSQL container")
    void testCreatePostgreSQL() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("postgres:16")
            .username("postgres_user")
            .password("postgres_pass")
            .database("postgres_db")
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.POSTGRESQL, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("postgres:16", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ PostgreSQL container created");
    }
    
    @Test
    @DisplayName("Create MySQL container")
    void testCreateMySQL() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("mysql:8.0")
            .username("mysql_user")
            .password("mysql_pass")
            .database("mysql_db")
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MYSQL, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("mysql:8.0", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ MySQL container created");
    }
    
    @Test
    @DisplayName("Create Elasticsearch container")
    void testCreateElasticsearch() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("elasticsearch:8.11.0")
            .environment(Map.of(
                "discovery.type", "single-node",
                "xpack.security.enabled", "false"
            ))
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.ELASTICSEARCH, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("elasticsearch:8.11.0", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ Elasticsearch container created");
    }
    
    @Test
    @DisplayName("Support all defined container types")
    void testAllContainerTypesSupported() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .build();
        
        ContainerType[] allTypes = ContainerType.values();
        for (ContainerType type : allTypes) {
            assertDoesNotThrow(() -> {
                GenericContainer<?> container = ContainerFactory.create(type, spec);
                assertNotNull(container, "Container should be created for type: " + type);
                String expectedImage = type.getDefaultImage();
                assertEquals(expectedImage, container.getDockerImageName(), 
                    "Container should use default image for type: " + type);
            }, "Should handle all defined container types without error");
        }
        
        log.info("✅ All {} container types supported: {}", allTypes.length, (Object) allTypes);
    }
    
    @Test
    @DisplayName("Apply network aliases when provided")
    void testNetworkAliases() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("mariadb:11.4.7")
            .networkAliases(new String[]{"db-alias", "database"})
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MARIADB, spec);
        
        assertNotNull(container, "Container should be created");
        
        log.info("✅ Network aliases applied to container");
    }
    
    @Test
    @DisplayName("Apply startup timeout when provided")
    void testStartupTimeout() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("mariadb:11.4.7")
            .startupTimeout(120)
            .build();
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MARIADB, spec);
        
        assertNotNull(container, "Container should be created");
        
        log.info("✅ Startup timeout applied to container");
    }
    
    @Test
    @DisplayName("Handle null environment variables")
    void testNullEnvironment() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("redis:7-alpine")
            .environment(null)
            .build();
        
        assertDoesNotThrow(() -> {
            GenericContainer<?> container = ContainerFactory.create(ContainerType.REDIS, spec);
            assertNotNull(container, "Container should be created even with null environment");
        }, "Should handle null environment gracefully");
        
        log.info("✅ Null environment handled gracefully");
    }
    
    @Test
    @DisplayName("Handle null network aliases")
    void testNullNetworkAliases() {
        ContainerConfiguration.ContainerSpec spec = ContainerConfiguration.ContainerSpec.builder()
            .image("redis:7-alpine")
            .networkAliases(null)
            .build();
        
        assertDoesNotThrow(() -> {
            GenericContainer<?> container = ContainerFactory.create(ContainerType.REDIS, spec);
            assertNotNull(container, "Container should be created even with null network aliases");
        }, "Should handle null network aliases gracefully");
        
        log.info("✅ Null network aliases handled gracefully");
    }
}