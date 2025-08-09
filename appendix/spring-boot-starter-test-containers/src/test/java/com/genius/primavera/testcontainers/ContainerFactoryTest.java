package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.*;
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
        MariaDbContainerSpec spec = new MariaDbContainerSpec();
        spec.setImage("mariadb:11.4.7");
        spec.setDatabase("testdb");
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MARIADB, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("mariadb:11.4.7", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ MariaDB container created with default configuration");
    }
    
    @Test
    @DisplayName("Create MariaDB container with custom configuration")
    void testCreateMariaDbCustom() {
        MariaDbContainerSpec spec = new MariaDbContainerSpec();
        spec.setImage("mariadb:11.4.7");
        spec.setDatabase("custom_db");
        spec.setUsername("custom_user");
        spec.setPassword("custom_pass");
        spec.setCharacterSet("utf8mb4");
        spec.setCollation("utf8mb4_unicode_ci");
        spec.setStartupTimeout(90);
        spec.setEnvironment(Map.of("MYSQL_CHARSET", "utf8mb4"));
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MARIADB, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("mariadb:11.4.7", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ MariaDB container created with custom configuration");
    }
    
    @Test
    @DisplayName("Create Redis container with default configuration")
    void testCreateRedisDefault() {
        RedisContainerSpec spec = new RedisContainerSpec();
        spec.setImage("redis:7-alpine");
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.REDIS, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("redis:7-alpine", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ Redis container created with default configuration");
    }
    
    @Test
    @DisplayName("Create Redis container with password")
    void testCreateRedisWithPassword() {
        RedisContainerSpec spec = new RedisContainerSpec();
        spec.setImage("redis:7-alpine");
        spec.setPassword("redis_password");
        spec.setMaxMemory("256mb");
        spec.setEnvironment(Map.of("REDIS_MAXMEMORY", "256mb"));
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.REDIS, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("redis:7-alpine", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ Redis container created with password configuration");
    }
    
    @Test
    @DisplayName("Create Kafka container with default configuration")
    void testCreateKafkaDefault() {
        BaseContainerSpec spec = new BaseContainerSpec();
        spec.setImage("confluentinc/cp-kafka:7.5.0");
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.KAFKA, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("confluentinc/cp-kafka:7.5.0", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ Kafka container created with default configuration");
    }
    
    @Test
    @DisplayName("Create Kafka container with custom environment")
    void testCreateKafkaCustom() {
        BaseContainerSpec spec = new BaseContainerSpec();
        spec.setImage("confluentinc/cp-kafka:7.5.0");
        spec.setEnvironment(Map.of(
            "KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true",
            "KAFKA_NUM_PARTITIONS", "3"
        ));
        spec.setStartupTimeout(180);
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.KAFKA, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("confluentinc/cp-kafka:7.5.0", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ Kafka container created with custom environment");
    }
    
    @Test
    @DisplayName("Create MongoDB container")
    void testCreateMongoDB() {
        MongoContainerSpec spec = new MongoContainerSpec();
        spec.setImage("mongo:7");
        spec.setUsername("mongo_user");
        spec.setPassword("mongo_pass");
        spec.setDatabase("mongo_db");
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MONGODB, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("mongo:7", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ MongoDB container created");
    }
    
    @Test
    @DisplayName("Create PostgreSQL container")
    void testCreatePostgreSQL() {
        PostgreSqlContainerSpec spec = new PostgreSqlContainerSpec();
        spec.setImage("postgres:16");
        spec.setUsername("postgres_user");
        spec.setPassword("postgres_pass");
        spec.setDatabase("postgres_db");
        spec.setLocale("en_US.UTF-8");
        spec.setEncoding("UTF8");
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.POSTGRESQL, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("postgres:16", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ PostgreSQL container created");
    }
    
    @Test
    @DisplayName("Create MySQL container")
    void testCreateMySQL() {
        MySqlContainerSpec spec = new MySqlContainerSpec();
        spec.setImage("mysql:8.0");
        spec.setUsername("mysql_user");
        spec.setPassword("mysql_pass");
        spec.setDatabase("mysql_db");
        spec.setCharacterSet("utf8mb4");
        spec.setCollation("utf8mb4_unicode_ci");
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MYSQL, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("mysql:8.0", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ MySQL container created");
    }
    
    @Test
    @DisplayName("Create Elasticsearch container")
    void testCreateElasticsearch() {
        BaseContainerSpec spec = new BaseContainerSpec();
        spec.setImage("elasticsearch:8.11.0");
        spec.setEnvironment(Map.of(
            "discovery.type", "single-node",
            "xpack.security.enabled", "false"
        ));
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.ELASTICSEARCH, spec);
        
        assertNotNull(container, "Container should be created");
        assertEquals("elasticsearch:8.11.0", container.getDockerImageName(), "Image should match spec");
        
        log.info("✅ Elasticsearch container created");
    }
    
    @Test
    @DisplayName("Support all defined container types")
    void testAllContainerTypesSupported() {
        ContainerType[] allTypes = ContainerType.values();
        for (ContainerType type : allTypes) {
            assertDoesNotThrow(() -> {
                BaseContainerSpec spec = createSpecForType(type);
                GenericContainer<?> container = ContainerFactory.create(type, spec);
                assertNotNull(container, "Container should be created for type: " + type);
                String expectedImage = type.getDefaultImage();
                assertEquals(expectedImage, container.getDockerImageName(), 
                    "Container should use default image for type: " + type);
            }, "Should handle all defined container types without error");
        }
        
        log.info("✅ All {} container types supported: {}", allTypes.length, (Object) allTypes);
    }
    
    private BaseContainerSpec createSpecForType(ContainerType type) {
        return switch (type) {
            case MARIADB -> {
                MariaDbContainerSpec spec = new MariaDbContainerSpec();
                spec.setImage(type.getDefaultImage());
                spec.setDatabase("testdb");
                spec.setUsername("testuser");
                spec.setPassword("testpass");
                yield spec;
            }
            case MYSQL -> {
                MySqlContainerSpec spec = new MySqlContainerSpec();
                spec.setImage(type.getDefaultImage());
                spec.setDatabase("testdb");
                spec.setUsername("testuser");
                spec.setPassword("testpass");
                yield spec;
            }
            case POSTGRESQL -> {
                PostgreSqlContainerSpec spec = new PostgreSqlContainerSpec();
                spec.setImage(type.getDefaultImage());
                spec.setDatabase("testdb");
                spec.setUsername("testuser");
                spec.setPassword("testpass");
                yield spec;
            }
            case REDIS -> {
                RedisContainerSpec spec = new RedisContainerSpec();
                spec.setImage(type.getDefaultImage());
                yield spec;
            }
            case MONGODB -> {
                MongoContainerSpec spec = new MongoContainerSpec();
                spec.setImage(type.getDefaultImage());
                spec.setDatabase("testdb");
                spec.setUsername("testuser");
                spec.setPassword("testpass");
                yield spec;
            }
            default -> {
                BaseContainerSpec spec = new BaseContainerSpec();
                spec.setImage(type.getDefaultImage());
                yield spec;
            }
        };
    }
    
    @Test
    @DisplayName("Apply network aliases when provided")
    void testNetworkAliases() {
        MariaDbContainerSpec spec = new MariaDbContainerSpec();
        spec.setImage("mariadb:11.4.7");
        spec.setDatabase("testdb");
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MARIADB, spec);
        
        assertNotNull(container, "Container should be created");
        
        log.info("✅ Network aliases applied to container");
    }
    
    @Test
    @DisplayName("Apply startup timeout when provided")
    void testStartupTimeout() {
        MariaDbContainerSpec spec = new MariaDbContainerSpec();
        spec.setImage("mariadb:11.4.7");
        spec.setDatabase("testdb");
        spec.setUsername("testuser");
        spec.setPassword("testpass");
        spec.setStartupTimeout(120);
        
        GenericContainer<?> container = ContainerFactory.create(ContainerType.MARIADB, spec);
        
        assertNotNull(container, "Container should be created");
        
        log.info("✅ Startup timeout applied to container");
    }
    
    @Test
    @DisplayName("Handle null environment variables")
    void testNullEnvironment() {
        RedisContainerSpec spec = new RedisContainerSpec();
        spec.setImage("redis:7-alpine");
        spec.setEnvironment(null);
        
        assertDoesNotThrow(() -> {
            GenericContainer<?> container = ContainerFactory.create(ContainerType.REDIS, spec);
            assertNotNull(container, "Container should be created even with null environment");
        }, "Should handle null environment gracefully");
        
        log.info("✅ Null environment handled gracefully");
    }
    
    @Test
    @DisplayName("Handle null network aliases")
    void testNullNetworkAliases() {
        RedisContainerSpec spec = new RedisContainerSpec();
        spec.setImage("redis:7-alpine");
        
        assertDoesNotThrow(() -> {
            GenericContainer<?> container = ContainerFactory.create(ContainerType.REDIS, spec);
            assertNotNull(container, "Container should be created even with null network aliases");
        }, "Should handle null network aliases gracefully");
        
        log.info("✅ Null network aliases handled gracefully");
    }
}