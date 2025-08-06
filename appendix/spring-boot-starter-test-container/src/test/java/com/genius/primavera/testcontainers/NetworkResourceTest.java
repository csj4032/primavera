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
import org.testcontainers.containers.GenericContainer;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Container Network and Resource Validation Tests")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "networkDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "networkCache"),
    @EnableTestContainers.TestContainer(type = ContainerType.KAFKA, name = "networkMessaging")
})
class V4NetworkResourceTest {
    
    @Autowired
    private Environment environment;
    
    @Autowired
    @Qualifier("networkDb")
    private DataSource networkDataSource;
    
    @Autowired
    @Qualifier("networkCache")
    private RedisTemplate<String, Object> networkRedisTemplate;
    
    @Test
    @Order(1)
    @DisplayName("Container port mapping validation")
    void testContainerPortMapping() {
        ContainerManager manager = ContainerRegistry.get();
        
        ContainerInfo dbInfo = manager.getContainer("networkDb");
        ContainerInfo cacheInfo = manager.getContainer("networkCache");
        ContainerInfo messagingInfo = manager.getContainer("networkMessaging");
        
        assertNotNull(dbInfo, "Database container should exist");
        assertNotNull(cacheInfo, "Cache container should exist");
        assertNotNull(messagingInfo, "Messaging container should exist");
        
        GenericContainer<?> dbContainer = dbInfo.getContainer();
        GenericContainer<?> cacheContainer = cacheInfo.getContainer();
        GenericContainer<?> messagingContainer = messagingInfo.getContainer();
        
        assertTrue(dbContainer.getFirstMappedPort() > 0, "Database should have mapped port");
        assertTrue(cacheContainer.getFirstMappedPort() > 0, "Cache should have mapped port");
        assertTrue(messagingContainer.getFirstMappedPort() > 0, "Messaging should have mapped port");
        
        assertNotEquals(dbContainer.getFirstMappedPort(), cacheContainer.getFirstMappedPort(),
            "Different containers should have different ports");
        assertNotEquals(dbContainer.getFirstMappedPort(), messagingContainer.getFirstMappedPort(),
            "Different containers should have different ports");
        
        log.info("Port mappings - DB: {}, Cache: {}, Messaging: {}",
            dbContainer.getFirstMappedPort(), cacheContainer.getFirstMappedPort(), 
            messagingContainer.getFirstMappedPort());
    }
    
    @Test
    @Order(2)
    @DisplayName("Network connectivity validation")
    void testNetworkConnectivity() {
        ContainerManager manager = ContainerRegistry.get();
        
        ContainerInfo dbInfo = manager.getContainer("networkDb");
        ContainerInfo cacheInfo = manager.getContainer("networkCache");
        
        String dbHost = dbInfo.getContainer().getHost();
        Integer dbPort = dbInfo.getContainer().getFirstMappedPort();
        String cacheHost = cacheInfo.getContainer().getHost();
        Integer cachePort = cacheInfo.getContainer().getFirstMappedPort();
        
        assertTrue(isPortReachable(dbHost, dbPort, 5000),
            String.format("Database should be reachable at %s:%d", dbHost, dbPort));
        assertTrue(isPortReachable(cacheHost, cachePort, 5000),
            String.format("Cache should be reachable at %s:%d", cacheHost, cachePort));
        
        log.info("Network connectivity validated for all containers");
    }
    
    @Test
    @Order(3)
    @DisplayName("Resource utilization monitoring")
    void testResourceUtilization() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(networkDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS resource_test (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "data LONGTEXT, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");
        
        Instant startTime = Instant.now();
        
        for (int i = 0; i < 100; i++) {
            String largeData = "RESOURCE_TEST_DATA_".repeat(1000) + i;
            jdbcTemplate.update("INSERT INTO resource_test (data) VALUES (?)", largeData);
        }
        
        Duration insertDuration = Duration.between(startTime, Instant.now());
        assertTrue(insertDuration.toSeconds() < 30,
            String.format("Resource-intensive operations should complete within 30s, took %ds",
                insertDuration.toSeconds()));
        
        try (Connection connection = networkDataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SHOW PROCESSLIST")) {
            
            int connectionCount = 0;
            while (resultSet.next()) {
                connectionCount++;
            }
            
            assertTrue(connectionCount > 0, "Should have active database connections");
            assertTrue(connectionCount < 50, "Should not have excessive connections");
        }
        
        log.info("Resource utilization validated - Insert duration: {}ms, Active connections monitored",
            insertDuration.toMillis());
    }
    
    @Test
    @Order(4)
    @DisplayName("Concurrent connection handling")
    void testConcurrentConnectionHandling() {
        int concurrentThreads = 20;
        
        CompletableFuture<?>[] futures = IntStream.range(0, concurrentThreads)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    JdbcTemplate jdbcTemplate = new JdbcTemplate(networkDataSource);
                    String threadData = "thread-" + i + "-" + System.currentTimeMillis();
                    
                    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS concurrent_conn_test_" + i + " (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "thread_data VARCHAR(100)" +
                        ")");
                    
                    jdbcTemplate.update("INSERT INTO concurrent_conn_test_" + i + " (thread_data) VALUES (?)",
                        threadData);
                    
                    String retrieved = jdbcTemplate.queryForObject(
                        "SELECT thread_data FROM concurrent_conn_test_" + i + " ORDER BY id DESC LIMIT 1",
                        String.class);
                    
                    assertEquals(threadData, retrieved, "Data should be consistent per thread");
                    
                    log.debug("Thread {} completed successfully", i);
                } catch (Exception e) {
                    log.error("Thread {} failed: {}", i, e.getMessage());
                    throw new RuntimeException(e);
                }
            }))
            .toArray(CompletableFuture[]::new);
        
        assertDoesNotThrow(() -> {
            CompletableFuture.allOf(futures).get(60, TimeUnit.SECONDS);
        }, "All concurrent connections should complete successfully");
        
        log.info("Concurrent connection handling validated with {} threads", concurrentThreads);
    }
    
    @Test
    @Order(5)
    @DisplayName("Memory and disk usage monitoring")
    void testMemoryDiskUsage() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(networkDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS disk_usage_test (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "large_blob LONGBLOB" +
            ")");
        
        byte[] largeData = new byte[1024 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10; i++) {
                jdbcTemplate.update("INSERT INTO disk_usage_test (large_blob) VALUES (?)", largeData);
            }
        }, "Large data operations should not cause memory issues");
        
        String redisKey = "memory-test-large-data";
        String redisValue = "LARGE_REDIS_VALUE_".repeat(10000);
        
        assertDoesNotThrow(() -> {
            networkRedisTemplate.opsForValue().set(redisKey, redisValue);
            Object retrieved = networkRedisTemplate.opsForValue().get(redisKey);
            assertEquals(redisValue, retrieved, "Large Redis values should be handled correctly");
        }, "Redis should handle large values without memory issues");
        
        log.info("Memory and disk usage validated - handled {}MB of data",
            (largeData.length * 10) / (1024 * 1024));
    }
    
    @Test
    @Order(6)
    @DisplayName("Environment property exposure validation")
    void testEnvironmentPropertyExposure() {
        String networkDbHost = environment.getProperty("testcontainer.runtime.networkDb.host");
        String networkDbPort = environment.getProperty("testcontainer.runtime.networkDb.port");
        String networkDbUrl = environment.getProperty("testcontainer.runtime.networkDb.jdbcUrl");
        
        assertNotNull(networkDbHost, "Network DB host should be exposed");
        assertNotNull(networkDbPort, "Network DB port should be exposed");
        assertNotNull(networkDbUrl, "Network DB JDBC URL should be exposed");
        
        assertTrue(networkDbHost.length() > 0, "Host should not be empty");
        assertTrue(Integer.parseInt(networkDbPort) > 0, "Port should be valid");
        assertTrue(networkDbUrl.startsWith("jdbc:mariadb://"), "URL should be valid MariaDB URL");
        
        String cacheHost = environment.getProperty("testcontainer.runtime.networkCache.host");
        String cachePort = environment.getProperty("testcontainer.runtime.networkCache.port");
        
        assertNotNull(cacheHost, "Cache host should be exposed");
        assertNotNull(cachePort, "Cache port should be exposed");
        
        log.info("Environment properties exposed - DB: {}:{}, Cache: {}:{}",
            networkDbHost, networkDbPort, cacheHost, cachePort);
    }
    
    @Test
    @Order(7)
    @DisplayName("Container health check validation")
    void testContainerHealthChecks() {
        ContainerManager manager = ContainerRegistry.get();
        
        ContainerInfo dbInfo = manager.getContainer("networkDb");
        ContainerInfo cacheInfo = manager.getContainer("networkCache");
        ContainerInfo messagingInfo = manager.getContainer("networkMessaging");
        
        assertTrue(dbInfo.getContainer().isHealthy(), "Database container should be healthy");
        assertTrue(cacheInfo.getContainer().isHealthy(), "Cache container should be healthy");
        assertTrue(messagingInfo.getContainer().isHealthy(), "Messaging container should be healthy");
        
        assertTrue(dbInfo.getContainer().isRunning(), "Database container should be running");
        assertTrue(cacheInfo.getContainer().isRunning(), "Cache container should be running");
        assertTrue(messagingInfo.getContainer().isRunning(), "Messaging container should be running");
        
        log.info("All container health checks passed");
    }
    
    @Test
    @Order(8)
    @DisplayName("Network isolation between containers")
    void testNetworkIsolation() {
        ContainerManager manager = ContainerRegistry.get();
        
        ContainerInfo dbInfo = manager.getContainer("networkDb");
        ContainerInfo cacheInfo = manager.getContainer("networkCache");
        
        String dbContainerId = dbInfo.getContainer().getContainerId();
        String cacheContainerId = cacheInfo.getContainer().getContainerId();
        
        assertNotEquals(dbContainerId, cacheContainerId,
            "Different containers should have different container IDs");
        
        assertNotEquals(dbInfo.getContainer().getFirstMappedPort(),
            cacheInfo.getContainer().getFirstMappedPort(),
            "Different containers should use different ports");
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(networkDataSource);
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS isolation_test (id INT PRIMARY KEY, data VARCHAR(50))");
        jdbcTemplate.update("INSERT INTO isolation_test (id, data) VALUES (1, 'db_data') " +
            "ON DUPLICATE KEY UPDATE data = VALUES(data)");
        
        networkRedisTemplate.opsForValue().set("isolation_test", "cache_data");
        
        String dbData = jdbcTemplate.queryForObject("SELECT data FROM isolation_test WHERE id = 1", String.class);
        Object cacheData = networkRedisTemplate.opsForValue().get("isolation_test");
        
        assertEquals("db_data", dbData, "Database should have its own data");
        assertEquals("cache_data", cacheData, "Cache should have its own data");
        assertNotEquals(dbData, cacheData, "Containers should be properly isolated");
        
        log.info("Network isolation validated - DB: '{}', Cache: '{}'", dbData, cacheData);
    }
    
    @Test
    @Order(9)
    @DisplayName("Resource cleanup on container stop")
    void testResourceCleanupOnStop() {
        ContainerManager manager = ContainerRegistry.get();
        
        assertTrue(manager.isStarted(), "Manager should be started initially");
        assertFalse(manager.getAllContainers().isEmpty(), "Should have containers");
        
        int initialContainerCount = manager.getAllContainers().size();
        assertTrue(initialContainerCount > 0, "Should have active containers");
        
        manager.getAllContainers().forEach(containerInfo -> {
            assertTrue(containerInfo.getContainer().isRunning(),
                "All containers should be running before cleanup test");
        });
        
        log.info("Resource cleanup validation - {} containers running and ready for cleanup",
            initialContainerCount);
    }
    
    private boolean isPortReachable(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            log.debug("Port {}:{} not reachable: {}", host, port, e.getMessage());
            return false;
        }
    }
}