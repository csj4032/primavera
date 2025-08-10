package com.genius.primavera.testcontainers;

import com.genius.primavera.testcontainers.config.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@DisplayName("ContainerConfiguration Unit Tests")
class ContainerConfigurationTest {

    @Test
    @DisplayName("MariaDB spec works correctly")
    void testMariaDbSpec() {
        MariaDbContainerSpec spec = new MariaDbContainerSpec();
        spec.setImage("mariadb:11.4.7");
        spec.setDatabase("test_db");
        spec.setUsername("test_user");
        spec.setPassword("test_pass");
        spec.setCharacterSet("utf8mb4");
        spec.setCollation("utf8mb4_unicode_ci");
        spec.setStartupTimeout(120);
        spec.setEnvironment(Map.of("ENV_VAR", "value"));

        assertEquals("mariadb:11.4.7", spec.getImage(), "Image should be set");
        assertEquals("test_db", spec.getDatabase(), "Database should be set");
        assertEquals("test_user", spec.getUsername(), "Username should be set");
        assertEquals("test_pass", spec.getPassword(), "Password should be set");
        assertEquals("utf8mb4", spec.getCharacterSet(), "Character set should be set");
        assertEquals("utf8mb4_unicode_ci", spec.getCollation(), "Collation should be set");
        assertEquals(120, spec.getStartupTimeout(), "Startup timeout should be set");
        assertNotNull(spec.getEnvironment(), "Environment should not be null");
        assertEquals("value", spec.getEnvironment().get("ENV_VAR"), "Environment variable should be set");

        log.info(" MariaDB spec works correctly");
    }

    @Test
    @DisplayName("MySQL spec works correctly")
    void testMySqlSpec() {
        MySqlContainerSpec spec = new MySqlContainerSpec();
        spec.setImage("mysql:8.0");
        spec.setDatabase("test_db");
        spec.setUsername("test_user");
        spec.setPassword("test_pass");
        spec.setCharacterSet("utf8mb4");
        spec.setCollation("utf8mb4_unicode_ci");
        spec.setSqlMode(MySqlContainerSpec.SqlMode.STRICT_TRANS_TABLES);
        spec.setDefaultStorageEngine(MySqlContainerSpec.StorageEngine.INNODB);
        spec.setMaxConnections(200);

        assertEquals("mysql:8.0", spec.getImage(), "Image should be set");
        assertEquals("test_db", spec.getDatabase(), "Database should be set");
        assertEquals("test_user", spec.getUsername(), "Username should be set");
        assertEquals("test_pass", spec.getPassword(), "Password should be set");
        assertEquals("utf8mb4", spec.getCharacterSet(), "Character set should be set");
        assertEquals(MySqlContainerSpec.SqlMode.STRICT_TRANS_TABLES, spec.getSqlMode(), "SQL mode should be set");
        assertEquals(200, spec.getMaxConnections(), "Max connections should be set");

        log.info(" MySQL spec works correctly");
    }

    @Test
    @DisplayName("PostgreSQL spec works correctly")
    void testPostgreSqlSpec() {
        PostgreSqlContainerSpec spec = new PostgreSqlContainerSpec();
        spec.setImage("postgres:16");
        spec.setDatabase("test_db");
        spec.setUsername("test_user");
        spec.setPassword("test_pass");
        spec.setLocale("en_US.UTF-8");
        spec.setEncoding("UTF8");
        spec.setSharedBuffers("256MB");
        spec.setWorkMem("8MB");
        spec.setMaxConnections(150);
        spec.setSslMode(PostgreSqlContainerSpec.SslMode.PREFER);

        assertEquals("postgres:16", spec.getImage(), "Image should be set");
        assertEquals("test_db", spec.getDatabase(), "Database should be set");
        assertEquals("test_user", spec.getUsername(), "Username should be set");
        assertEquals("test_pass", spec.getPassword(), "Password should be set");
        assertEquals("en_US.UTF-8", spec.getLocale(), "Locale should be set");
        assertEquals("UTF8", spec.getEncoding(), "Encoding should be set");
        assertEquals("256MB", spec.getSharedBuffers(), "Shared buffers should be set");
        assertEquals("8MB", spec.getWorkMem(), "Work memory should be set");
        assertEquals(150, spec.getMaxConnections(), "Max connections should be set");
        assertEquals(PostgreSqlContainerSpec.SslMode.PREFER, spec.getSslMode(), "SSL mode should be set");

        log.info(" PostgreSQL spec works correctly");
    }

    @Test
    @DisplayName("Redis spec works correctly")
    void testRedisSpec() {
        RedisContainerSpec spec = new RedisContainerSpec();
        spec.setImage("redis:7-alpine");
        spec.setPassword("redis_pass");
        spec.setMaxMemory("256mb");
        spec.setMaxMemoryPolicy(RedisContainerSpec.MaxMemoryPolicy.ALLKEYS_LRU);
        spec.setPersistenceEnabled(false);
        spec.setAppendOnlyEnabled(false);
        spec.setStartupTimeout(45);

        assertEquals("redis:7-alpine", spec.getImage(), "Image should be set");
        assertEquals("redis_pass", spec.getPassword(), "Password should be set");
        assertEquals("256mb", spec.getMaxMemory(), "Max memory should be set");
        assertEquals(RedisContainerSpec.MaxMemoryPolicy.ALLKEYS_LRU, spec.getMaxMemoryPolicy(), "Max memory policy should be set");
        assertFalse(spec.getPersistenceEnabled(), "Persistence should be disabled");
        assertFalse(spec.getAppendOnlyEnabled(), "Append only should be disabled");
        assertEquals(45, spec.getStartupTimeout(), "Startup timeout should be set");

        log.info(" Redis spec works correctly");
    }

    @Test
    @DisplayName("MongoDB spec works correctly")
    void testMongoSpec() {
        MongoContainerSpec spec = new MongoContainerSpec();
        spec.setImage("mongo:7");
        spec.setDatabase("test_db");
        spec.setUsername("test_user");
        spec.setPassword("test_pass");
        spec.setAuthDatabase("admin");
        spec.setReplicaSetName("rs0");
        spec.setShardingEnabled(false);
        spec.setWiredTigerCacheSizeMB(1024);
        spec.setJournalEnabled(true);
        spec.setStorageEngine(MongoContainerSpec.StorageEngine.WIRED_TIGER);

        assertEquals("mongo:7", spec.getImage(), "Image should be set");
        assertEquals("test_db", spec.getDatabase(), "Database should be set");
        assertEquals("test_user", spec.getUsername(), "Username should be set");
        assertEquals("test_pass", spec.getPassword(), "Password should be set");
        assertEquals("admin", spec.getAuthDatabase(), "Auth database should be set");
        assertEquals("rs0", spec.getReplicaSetName(), "Replica set name should be set");
        assertFalse(spec.getShardingEnabled(), "Sharding should be disabled");
        assertEquals(1024, spec.getWiredTigerCacheSizeMB(), "Cache size should be set");
        assertTrue(spec.getJournalEnabled(), "Journal should be enabled");
        assertEquals(MongoContainerSpec.StorageEngine.WIRED_TIGER, spec.getStorageEngine(), "Storage engine should be set");

        log.info(" MongoDB spec works correctly");
    }

    @Test
    @DisplayName("Base container spec works correctly")
    void testBaseContainerSpec() {
        BaseContainerSpec spec = new BaseContainerSpec() {
            {
                setImage("test:latest");
                setStartupTimeout(120);
                setEnvironment(Map.of("ENV_VAR", "value"));
            }
        };

        assertEquals("test:latest", spec.getImage(), "Image should be set");
        assertEquals(120, spec.getStartupTimeout(), "Startup timeout should be set");
        assertNotNull(spec.getEnvironment(), "Environment should not be null");
        assertEquals("value", spec.getEnvironment().get("ENV_VAR"), "Environment variable should be set");

        log.info(" Base container spec works correctly");
    }

    @Test
    @DisplayName("Container configuration with instance configs works")
    void testContainerInstanceConfigs() {
        ContainerConfiguration config = new ContainerConfiguration();
        
        ContainerConfiguration.ContainerInstanceConfig dbConfig = new ContainerConfiguration.ContainerInstanceConfig();
        dbConfig.setType(ContainerType.MARIADB);
        
        MariaDbContainerSpec mariadbSpec = new MariaDbContainerSpec();
        mariadbSpec.setImage("mariadb:11.4.7");
        mariadbSpec.setDatabase("testdb");
        mariadbSpec.setUsername("testuser");
        mariadbSpec.setPassword("testpass");
        dbConfig.setMariadb(mariadbSpec);
        
        ContainerConfiguration.ContainerInstanceConfig cacheConfig = new ContainerConfiguration.ContainerInstanceConfig();
        cacheConfig.setType(ContainerType.REDIS);
        
        RedisContainerSpec redisSpec = new RedisContainerSpec();
        redisSpec.setImage("redis:7-alpine");
        redisSpec.setPassword("redis_pass");
        cacheConfig.setRedis(redisSpec);
        
        Map<String, ContainerConfiguration.ContainerInstanceConfig> containers = Map.of(
                "database", dbConfig,
                "cache", cacheConfig
        );
        
        config.setContainers(containers);

        assertNotNull(config.getContainers(), "Containers map should not be null");
        assertEquals(2, config.getContainers().size(), "Should have 2 container configs");

        ContainerConfiguration.ContainerInstanceConfig retrievedDbConfig = config.getContainers().get("database");
        ContainerConfiguration.ContainerInstanceConfig retrievedCacheConfig = config.getContainers().get("cache");

        assertNotNull(retrievedDbConfig, "Database config should be retrievable");
        assertNotNull(retrievedCacheConfig, "Cache config should be retrievable");
        
        assertEquals(ContainerType.MARIADB, retrievedDbConfig.getType(), "Database type should be MARIADB");
        assertEquals(ContainerType.REDIS, retrievedCacheConfig.getType(), "Cache type should be REDIS");
        
        assertEquals("testdb", retrievedDbConfig.getMariadb().getDatabase(), "Database name should be correct");
        assertEquals("redis_pass", retrievedCacheConfig.getRedis().getPassword(), "Cache password should be correct");

        log.info(" Container instance configs work correctly");
    }

    @Test
    @DisplayName("Container configuration works correctly")
    void testContainerConfiguration() {
        ContainerConfiguration config = new ContainerConfiguration();
        
        assertNotNull(config.getContainers(), "Containers map should not be null");
        assertTrue(config.getContainers().isEmpty(), "Containers map should be empty by default");
        
        ContainerConfiguration.ContainerInstanceConfig instanceConfig = new ContainerConfiguration.ContainerInstanceConfig();
        instanceConfig.setType(ContainerType.MARIADB);
        
        MariaDbContainerSpec spec = new MariaDbContainerSpec();
        spec.setDatabase("test_db");
        instanceConfig.setMariadb(spec);
        
        config.getContainers().put("test", instanceConfig);
        
        assertEquals(1, config.getContainers().size(), "Should have 1 container config");
        assertTrue(config.getContainers().containsKey("test"), "Should contain test key");
        
        log.info(" Container configuration works correctly");
    }

    @Test
    @DisplayName("Spec for type retrieval works correctly")
    void testSpecForTypeRetrieval() {
        ContainerConfiguration.ContainerInstanceConfig config = new ContainerConfiguration.ContainerInstanceConfig();
        config.setType(ContainerType.MARIADB);
        
        MariaDbContainerSpec mariadbSpec = new MariaDbContainerSpec();
        mariadbSpec.setDatabase("test_mariadb");
        config.setMariadb(mariadbSpec);
        
        BaseContainerSpec retrievedSpec = config.getSpecForType();
        
        assertNotNull(retrievedSpec, "Retrieved spec should not be null");
        assertTrue(retrievedSpec instanceof MariaDbContainerSpec, "Retrieved spec should be MariaDbContainerSpec");
        assertEquals("test_mariadb", ((MariaDbContainerSpec) retrievedSpec).getDatabase(), "Database should match");

        log.info(" Spec for type retrieval works correctly");
    }
}