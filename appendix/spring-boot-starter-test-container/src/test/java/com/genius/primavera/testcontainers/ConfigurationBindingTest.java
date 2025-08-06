package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Configuration Binding and Property Tests")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "configPrimaryDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "configSecondaryDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "configCache")
})
class V4ConfigurationBindingTest {
    
    @Autowired
    private Environment environment;
    
    @Autowired 
    @Qualifier("configPrimaryDb")
    private DataSource primaryDataSource;
    
    @Autowired
    @Qualifier("configSecondaryDb")
    private DataSource secondaryDataSource;
    
    @Autowired
    @Qualifier("configCache")
    private RedisTemplate<String, Object> redisTemplate;
    
    @Test
    @Order(1)  
    @DisplayName("Application-test.yml property binding validation")
    void testConfigurationPropertyBinding() {
        ContainerManager manager = ContainerRegistry.get();
        
        ContainerInfo primaryInfo = manager.getContainer("configPrimaryDb");
        ContainerInfo secondaryInfo = manager.getContainer("configSecondaryDb");
        ContainerInfo cacheInfo = manager.getContainer("configCache");
        
        assertEquals("mariadb:11.4.7", primaryInfo.getSpec().getImage(), 
            "Primary DB should use configured image");
        assertEquals("primary_db", primaryInfo.getSpec().getDatabase(),
            "Primary DB should use configured database name");
        assertEquals("primary_user", primaryInfo.getSpec().getUsername(),
            "Primary DB should use configured username");
        assertEquals("primary_pass", primaryInfo.getSpec().getPassword(),
            "Primary DB should use configured password");
        
        assertEquals("secondary_db", secondaryInfo.getSpec().getDatabase(),
            "Secondary DB should use configured database name");
        assertEquals("secondary_user", secondaryInfo.getSpec().getUsername(),
            "Secondary DB should use configured username");
        
        assertEquals("redis:7-alpine", cacheInfo.getSpec().getImage(),
            "Cache should use configured Redis image");
        assertEquals("redis_password", cacheInfo.getSpec().getPassword(),
            "Cache should use configured password");
        
        log.info("All configuration properties bound correctly");
    }
    
    @Test
    @Order(2)
    @DisplayName("Environment variable injection validation")
    void testEnvironmentVariables() {
        ContainerManager manager = ContainerRegistry.get();
        
        ContainerInfo primaryInfo = manager.getContainer("primaryDb");
        ContainerInfo cacheInfo = manager.getContainer("cache");
        
        Map<String, String> primaryEnv = primaryInfo.getSpec().getEnvironment();
        assertNotNull(primaryEnv, "Primary DB environment should not be null");
        assertEquals("utf8mb4", primaryEnv.get("MYSQL_CHARSET"),
            "Primary DB should have configured charset");
        assertEquals("utf8mb4_unicode_ci", primaryEnv.get("MYSQL_COLLATION"),
            "Primary DB should have configured collation");
        
        Map<String, String> cacheEnv = cacheInfo.getSpec().getEnvironment();
        assertNotNull(cacheEnv, "Cache environment should not be null");
        assertEquals("256mb", cacheEnv.get("REDIS_MAXMEMORY"),
            "Cache should have configured max memory");
        assertEquals("allkeys-lru", cacheEnv.get("REDIS_MAXMEMORY_POLICY"),
            "Cache should have configured eviction policy");
        
        log.info("Environment variables properly injected");
    }
    
    @Test
    @Order(3)
    @DisplayName("Network aliases configuration validation")
    void testNetworkAliases() {
        ContainerManager manager = ContainerRegistry.get();
        
        ContainerInfo primaryInfo = manager.getContainer("primaryDb");
        ContainerInfo secondaryInfo = manager.getContainer("secondaryDb");
        
        assertTrue(Arrays.asList(primaryInfo.getSpec().getNetworkAliases()).contains("primary-db"),
            "Primary DB should have primary-db alias");
        assertTrue(Arrays.asList(primaryInfo.getSpec().getNetworkAliases()).contains("main-db"),
            "Primary DB should have main-db alias");
        
        assertTrue(Arrays.asList(secondaryInfo.getSpec().getNetworkAliases()).contains("secondary-db"),
            "Secondary DB should have secondary-db alias");
        assertTrue(Arrays.asList(secondaryInfo.getSpec().getNetworkAliases()).contains("replica-db"),
            "Secondary DB should have replica-db alias");
        
        log.info("Network aliases configured correctly");
    }
    
    @Test
    @Order(4)
    @DisplayName("Startup timeout configuration validation")
    void testStartupTimeouts() {
        ContainerManager manager = ContainerRegistry.get();
        
        ContainerInfo primaryInfo = manager.getContainer("configPrimaryDb");
        ContainerInfo secondaryInfo = manager.getContainer("configSecondaryDb");
        ContainerInfo cacheInfo = manager.getContainer("configCache");
        
        assertEquals(120, primaryInfo.getSpec().getStartupTimeout(),
            "Primary DB should have 120s startup timeout");
        assertEquals(120, secondaryInfo.getSpec().getStartupTimeout(),
            "Secondary DB should have 120s startup timeout");
        assertEquals(60, cacheInfo.getSpec().getStartupTimeout(),
            "Cache should have 60s startup timeout");
        
        log.info("Startup timeouts configured correctly");
    }
    
    @Test
    @Order(5)
    @DisplayName("Runtime property registration validation")
    void testRuntimeProperties() {
        String primaryDbUrl = environment.getProperty("testcontainer.runtime.configPrimaryDb.jdbcUrl");
        String primaryDbHost = environment.getProperty("testcontainer.runtime.configPrimaryDb.host");
        String primaryDbPort = environment.getProperty("testcontainer.runtime.configPrimaryDb.port");
        
        assertNotNull(primaryDbUrl, "Primary DB JDBC URL should be registered");
        assertNotNull(primaryDbHost, "Primary DB host should be registered");
        assertNotNull(primaryDbPort, "Primary DB port should be registered");
        
        assertTrue(primaryDbUrl.startsWith("jdbc:mariadb://"),
            "Primary DB URL should be valid MariaDB URL");
        assertTrue(Integer.parseInt(primaryDbPort) > 0,
            "Primary DB port should be valid port number");
        
        String cacheHost = environment.getProperty("testcontainer.runtime.configCache.host");
        String cachePort = environment.getProperty("testcontainer.runtime.configCache.port");
        
        assertNotNull(cacheHost, "Cache host should be registered");
        assertNotNull(cachePort, "Cache port should be registered");
        
        log.info("Runtime properties registered: primaryDb={}:{}, cache={}:{}",
            primaryDbHost, primaryDbPort, cacheHost, cachePort);
    }
    
    @Test
    @Order(6)
    @DisplayName("Database credential validation against configuration")
    void testDatabaseCredentials() throws Exception {
        try (Connection primaryConn = primaryDataSource.getConnection()) {
            DatabaseMetaData primaryMeta = primaryConn.getMetaData();
            String primaryUrl = primaryMeta.getURL();
            String primaryUser = primaryMeta.getUserName();
            
            assertTrue(primaryUrl.contains("primary_db"),
                "Primary connection should use configured database name");
            assertEquals("primary_user", primaryUser,
                "Primary connection should use configured username");
        }
        
        try (Connection secondaryConn = secondaryDataSource.getConnection()) {
            DatabaseMetaData secondaryMeta = secondaryConn.getMetaData();
            String secondaryUrl = secondaryMeta.getURL();
            String secondaryUser = secondaryMeta.getUserName();
            
            assertTrue(secondaryUrl.contains("secondary_db"),
                "Secondary connection should use configured database name");
            assertEquals("secondary_user", secondaryUser,
                "Secondary connection should use configured username");
        }
        
        log.info("Database credentials validated against configuration");
    }
    
    @Test
    @Order(7)
    @DisplayName("Redis authentication validation")
    void testRedisAuthentication() {
        String testKey = "config-auth-test";
        String testValue = "authenticated-" + System.currentTimeMillis();
        
        assertDoesNotThrow(() -> {
            redisTemplate.opsForValue().set(testKey, testValue);
            Object retrieved = redisTemplate.opsForValue().get(testKey);
            assertEquals(testValue, retrieved, "Redis should authenticate with configured password");
        }, "Redis operations should work with configured authentication");
        
        log.info("Redis authentication validated with configured password");
    }
    
    @Test
    @Order(8)
    @DisplayName("Configuration fallback to defaults validation")
    void testConfigurationDefaults() {
        ContainerManager manager = ContainerRegistry.get();
        
        ContainerInfo primaryInfo = manager.getContainer("primaryDb");
        
        assertEquals(ContainerType.MARIADB, primaryInfo.getType(),
            "Container type should match annotation");
        assertEquals("primaryDb", primaryInfo.getName(),
            "Container name should match annotation");
        
        assertNotNull(primaryInfo.getContainer(),
            "Container instance should be created");
        assertTrue(primaryInfo.getContainer().isRunning(),
            "Container should be running after configuration");
        
        log.info("Configuration and defaults properly applied");
    }
    
    @Test
    @Order(9)
    @DisplayName("Cross-database isolation validation")
    void testCrossDatabaseIsolation() {
        JdbcTemplate primaryJdbc = new JdbcTemplate(primaryDataSource);
        JdbcTemplate secondaryJdbc = new JdbcTemplate(secondaryDataSource);
        
        primaryJdbc.execute("CREATE TABLE IF NOT EXISTS config_isolation_primary (id INT PRIMARY KEY, data VARCHAR(50))");
        secondaryJdbc.execute("CREATE TABLE IF NOT EXISTS config_isolation_secondary (id INT PRIMARY KEY, data VARCHAR(50))");
        
        primaryJdbc.update("INSERT INTO config_isolation_primary (id, data) VALUES (1, 'primary-data')");
        secondaryJdbc.update("INSERT INTO config_isolation_secondary (id, data) VALUES (1, 'secondary-data')");
        
        String primaryData = primaryJdbc.queryForObject(
            "SELECT data FROM config_isolation_primary WHERE id = 1", String.class);
        String secondaryData = secondaryJdbc.queryForObject(
            "SELECT data FROM config_isolation_secondary WHERE id = 1", String.class);
        
        assertEquals("primary-data", primaryData, "Primary DB should have its own data");
        assertEquals("secondary-data", secondaryData, "Secondary DB should have its own data");
        assertNotEquals(primaryData, secondaryData, "Databases should be isolated");
        
        log.info("Cross-database isolation validated: primary='{}', secondary='{}'",
            primaryData, secondaryData);
    }
}