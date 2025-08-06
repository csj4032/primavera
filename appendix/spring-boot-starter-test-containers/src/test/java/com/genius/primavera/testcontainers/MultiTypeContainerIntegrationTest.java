package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Multi-type Container Integration Tests
 * - MariaDB, PostgreSQL, Redis integration
 * - Cross-database data synchronization
 * - Caching strategy validation
 * - Transaction consistency tests
 * - Inter-container network communication
 * - Complex workflow validation
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Multi-type Container Integration Tests")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "primaryDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.POSTGRESQL, name = "analyticsDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "cacheStore")
})
class MultiTypeContainerIntegrationTest {

    @Autowired
    @Qualifier("primaryDb")
    private DataSource primaryDataSource;

    @Autowired
    @Qualifier("analyticsDb")
    private DataSource analyticsDataSource;

    @Autowired
    @Qualifier("cacheStore")
    private RedisTemplate<String, Object> redisTemplate;

    private JdbcTemplate primaryJdbc;
    private JdbcTemplate analyticsJdbc;
    private ContainerManager containerManager;

    @BeforeAll
    void setupMultiTypeTests() {
        primaryJdbc = new JdbcTemplate(primaryDataSource);
        analyticsJdbc = new JdbcTemplate(analyticsDataSource);
        containerManager = ContainerRegistry.get();

        // MariaDB schema creation
        primaryJdbc.execute("""
            CREATE TABLE users (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                email VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """);

        primaryJdbc.execute("""
            CREATE TABLE orders (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                product_name VARCHAR(200) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status VARCHAR(20) DEFAULT 'PENDING',
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """);

        // PostgreSQL analytics schema creation
        analyticsJdbc.execute("""
            CREATE TABLE user_analytics (
                id BIGSERIAL PRIMARY KEY,
                user_id BIGINT NOT NULL,
                event_type VARCHAR(50) NOT NULL,
                event_data JSONB,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        analyticsJdbc.execute("""
            CREATE TABLE daily_summary (
                summary_date DATE PRIMARY KEY,
                total_users BIGINT DEFAULT 0,
                total_orders BIGINT DEFAULT 0,
                total_revenue DECIMAL(15,2) DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Insert initial test data
        primaryJdbc.update("""
            INSERT INTO users (username, email) VALUES 
            ('alice', 'alice@example.com'),
            ('bob', 'bob@example.com'),
            ('charlie', 'charlie@example.com')
        """);

        log.info("Multi-type container integration test environment initialized");
    }

    @Test
    @Order(1)
    @DisplayName("All container types connectivity and status verification")
    void testAllContainerTypesConnectivity() {
        // MariaDB connection verification
        assertDoesNotThrow(() -> {
            String mariaVersion = primaryJdbc.queryForObject("SELECT VERSION()", String.class);
            assertNotNull(mariaVersion, "MariaDB version info should be available");
            assertTrue(mariaVersion.toLowerCase().contains("mariadb"), "Should be MariaDB version");
            log.info("MariaDB version: {}", mariaVersion);
        }, "MariaDB connection should succeed");

        // PostgreSQL connection verification
        assertDoesNotThrow(() -> {
            String pgVersion = analyticsJdbc.queryForObject("SELECT version()", String.class);
            assertNotNull(pgVersion, "PostgreSQL version info should be available");
            assertTrue(pgVersion.toLowerCase().contains("postgresql"), "Should be PostgreSQL version");
            log.info("PostgreSQL version: {}", pgVersion.substring(0, 50) + "...");
        }, "PostgreSQL connection should succeed");

        // Redis connection verification
        assertDoesNotThrow(() -> {
            redisTemplate.opsForValue().set("connectivity_test", "redis_connected");
            String result = (String) redisTemplate.opsForValue().get("connectivity_test");
            assertEquals("redis_connected", result, "Redis store/retrieve should succeed");
            log.info("Redis connection verified");
        }, "Redis connection should succeed");

        // Container manager status verification
        ContainerInfo primaryInfo = containerManager.getContainer("primaryDb");
        ContainerInfo analyticsInfo = containerManager.getContainer("analyticsDb");
        ContainerInfo cacheInfo = containerManager.getContainer("cacheStore");

        assertTrue(primaryInfo.container().isRunning(), "Primary DB should be running");
        assertTrue(analyticsInfo.container().isRunning(), "Analytics DB should be running");
        assertTrue(cacheInfo.container().isRunning(), "Cache should be running");

        log.info("All container types connectivity verified");
    }

    @Test
    @Order(2)
    @DisplayName("Cross-database data synchronization")
    void testMultiDatabaseSynchronization() {
        // Insert order data in Primary DB
        Long userId = 1L;
        int orderResult = primaryJdbc.update(
            "INSERT INTO orders (user_id, product_name, amount, status) VALUES (?, ?, ?, ?)",
            userId, "Laptop", 1299.99, "COMPLETED");
        assertEquals(1, orderResult, "Order insertion should succeed");

        // Query order information from Primary DB
        Map<String, Object> orderData = primaryJdbc.queryForMap("""
            SELECT o.id, u.username, u.email, o.product_name, o.amount, o.status
            FROM orders o 
            JOIN users u ON o.user_id = u.id 
            WHERE o.user_id = ? 
            ORDER BY o.id DESC 
            LIMIT 1
        """, userId);

        assertNotNull(orderData, "Order data should be retrieved");
        assertEquals("alice", orderData.get("username"), "Username should match");

        // Synchronize event data to Analytics DB
        String eventDataJson = String.format("""
            {"order_id": %s, "product_name": "%s", "amount": %.2f, "status": "%s"}
        """, orderData.get("id"), orderData.get("product_name"), 
            orderData.get("amount"), orderData.get("status"));

        int analyticsResult = analyticsJdbc.update(
            "INSERT INTO user_analytics (user_id, event_type, event_data) VALUES (?, ?, ?::jsonb)",
            userId, "ORDER_COMPLETED", eventDataJson);
        assertEquals(1, analyticsResult, "Analytics data insertion should succeed");

        // Verify data in Analytics DB
        Integer analyticsCount = analyticsJdbc.queryForObject(
            "SELECT COUNT(*) FROM user_analytics WHERE user_id = ? AND event_type = ?",
            Integer.class, userId, "ORDER_COMPLETED");
        assertEquals(1, analyticsCount, "Analytics data should be synchronized");

        // JSON data verification
        String jsonData = analyticsJdbc.queryForObject(
            "SELECT event_data::text FROM user_analytics WHERE user_id = ? AND event_type = ?",
            String.class, userId, "ORDER_COMPLETED");
        assertNotNull(jsonData, "JSON data should be stored");
        assertTrue(jsonData.contains("Laptop"), "Product name should be in JSON");

        log.info("Cross-database data synchronization completed");
    }

    @Test
    @Order(3)
    @DisplayName("Caching strategy and performance verification")
    void testCachingStrategyAndPerformance() {
        String cacheKey = "user_profile_";
        
        // Cache user information
        List<Map<String, Object>> users = primaryJdbc.queryForList("SELECT * FROM users");
        
        for (Map<String, Object> user : users) {
            String userCacheKey = cacheKey + user.get("id");
            
            // Cache user info in Redis
            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("id", user.get("id"));
            userProfile.put("username", user.get("username"));
            userProfile.put("email", user.get("email"));
            userProfile.put("cached_at", System.currentTimeMillis());
            
            redisTemplate.opsForHash().putAll(userCacheKey, userProfile);
            redisTemplate.expire(userCacheKey, 1, TimeUnit.HOURS);
        }

        // Retrieve and verify data from cache
        for (Map<String, Object> user : users) {
            String userCacheKey = cacheKey + user.get("id");
            
            Map<Object, Object> cachedUser = redisTemplate.opsForHash().entries(userCacheKey);
            assertFalse(cachedUser.isEmpty(), "Cached user info should exist");
            assertEquals(user.get("username"), cachedUser.get("username"), "Cached username should match");
            assertEquals(user.get("email"), cachedUser.get("email"), "Cached email should match");
        }

        // Cache performance test
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            String userCacheKey = cacheKey + (i % users.size() + 1);
            redisTemplate.opsForHash().entries(userCacheKey);
        }
        long cacheTime = System.currentTimeMillis() - startTime;

        // Direct database query performance test
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Long userIdForQuery = (long) (i % users.size() + 1);
            primaryJdbc.queryForMap("SELECT * FROM users WHERE id = ?", userIdForQuery);
        }
        long dbTime = System.currentTimeMillis() - startTime;

        log.info("Performance comparison - Cache: {}ms, DB: {}ms", cacheTime, dbTime);
        assertTrue(cacheTime <= dbTime, "Cache queries should be faster than or equal to DB queries");

        // Cache expiration and TTL verification
        String testCacheKey = cacheKey + "ttl_test";
        redisTemplate.opsForValue().set(testCacheKey, "test_value", 1, TimeUnit.SECONDS);
        
        assertTrue(redisTemplate.hasKey(testCacheKey), "Cache key should exist");
        
        // Wait 1.5 seconds and check expiration
        assertDoesNotThrow(() -> Thread.sleep(1500));
        assertFalse(redisTemplate.hasKey(testCacheKey), "Cache key should be deleted after TTL expiry");

        log.info("Caching strategy and performance verification completed");
    }

    @Test
    @Order(4)
    @DisplayName("Transaction consistency and complex workflow")
    void testTransactionConsistencyAndComplexWorkflow() {
        String workflowId = "workflow_" + System.currentTimeMillis();
        
        try {
            // Step 1: Create new user (Primary DB)
            int userResult = primaryJdbc.update(
                "INSERT INTO users (username, email) VALUES (?, ?)",
                workflowId, workflowId + "@test.com");
            assertEquals(1, userResult, "User creation should succeed");

            // Get created user ID
            Long newUserId = primaryJdbc.queryForObject(
                "SELECT id FROM users WHERE username = ?", Long.class, workflowId);
            assertNotNull(newUserId, "New user ID should exist");

            // Step 2: Create order (Primary DB)
            int orderResult = primaryJdbc.update(
                "INSERT INTO orders (user_id, product_name, amount, status) VALUES (?, ?, ?, ?)",
                newUserId, "Test Product", 99.99, "PENDING");
            assertEquals(1, orderResult, "Order creation should succeed");

            // Step 3: Record analytics event (Analytics DB)
            String registrationEventJson = String.format("""
                {"user_id": %d, "username": "%s", "email": "%s", "workflow_id": "%s"}
            """, newUserId, workflowId, workflowId + "@test.com", workflowId);

            int registrationEvent = analyticsJdbc.update(
                "INSERT INTO user_analytics (user_id, event_type, event_data) VALUES (?, ?, ?::jsonb)",
                newUserId, "USER_REGISTRATION", registrationEventJson);
            assertEquals(1, registrationEvent, "Registration event recording should succeed");

            // Step 4: Cache update (Redis)
            String userCacheKey = "new_user_" + newUserId;
            Map<String, Object> newUserProfile = Map.of(
                "id", newUserId,
                "username", workflowId,
                "email", workflowId + "@test.com",
                "status", "ACTIVE",
                "workflow_id", workflowId
            );
            
            redisTemplate.opsForHash().putAll(userCacheKey, newUserProfile);
            redisTemplate.expire(userCacheKey, 24, TimeUnit.HOURS);

            // Step 5: Workflow verification
            // Verify user and order info from Primary DB
            Map<String, Object> userOrder = primaryJdbc.queryForMap("""
                SELECT u.username, u.email, o.product_name, o.amount, o.status
                FROM users u
                LEFT JOIN orders o ON u.id = o.user_id
                WHERE u.id = ?
            """, newUserId);

            assertEquals(workflowId, userOrder.get("username"), "Username should match");
            assertEquals("Test Product", userOrder.get("product_name"), "Product name should match");

            // Verify event in Analytics DB
            Integer eventCount = analyticsJdbc.queryForObject(
                "SELECT COUNT(*) FROM user_analytics WHERE user_id = ?", 
                Integer.class, newUserId);
            assertEquals(1, eventCount, "Analytics event should be recorded");

            // Verify cache in Redis
            Map<Object, Object> cachedProfile = redisTemplate.opsForHash().entries(userCacheKey);
            assertEquals(workflowId, cachedProfile.get("username"), "Cached username should match");

            // Step 6: Complete order processing
            int updateResult = primaryJdbc.update(
                "UPDATE orders SET status = 'COMPLETED' WHERE user_id = ?", newUserId);
            assertEquals(1, updateResult, "Order status update should succeed");

            // Record completion event
            String completionEventJson = String.format("""
                {"user_id": %d, "order_completed": true, "workflow_id": "%s"}
            """, newUserId, workflowId);

            analyticsJdbc.update(
                "INSERT INTO user_analytics (user_id, event_type, event_data) VALUES (?, ?, ?::jsonb)",
                newUserId, "ORDER_COMPLETION", completionEventJson);

            // Final verification
            String finalOrderStatus = primaryJdbc.queryForObject(
                "SELECT status FROM orders WHERE user_id = ?", String.class, newUserId);
            assertEquals("COMPLETED", finalOrderStatus, "Order status should be completed");

            Integer totalEvents = analyticsJdbc.queryForObject(
                "SELECT COUNT(*) FROM user_analytics WHERE user_id = ?", 
                Integer.class, newUserId);
            assertEquals(2, totalEvents, "Total 2 events should be recorded");

            log.info("Complex workflow completed - User: {}, Order status: {}, Total events: {}", 
                workflowId, finalOrderStatus, totalEvents);

        } catch (Exception e) {
            log.error("Error during workflow execution", e);
            fail("Transaction consistency test failed: " + e.getMessage());
        }

        log.info("Transaction consistency and complex workflow verification completed");
    }

    @Test
    @Order(5)
    @DisplayName("Daily summary and analytics data generation")
    void testDailySummaryAndAnalytics() {
        // Generate summary data for current date
        String today = java.time.LocalDate.now().toString();
        
        // Query aggregate data from Primary DB
        Map<String, Object> primaryStats = primaryJdbc.queryForMap("""
            SELECT 
                COUNT(DISTINCT u.id) as total_users,
                COUNT(o.id) as total_orders,
                COALESCE(SUM(o.amount), 0) as total_revenue
            FROM users u
            LEFT JOIN orders o ON u.id = o.user_id
        """);

        Long totalUsers = ((Number) primaryStats.get("total_users")).longValue();
        Long totalOrders = ((Number) primaryStats.get("total_orders")).longValue();
        Double totalRevenue = ((Number) primaryStats.get("total_revenue")).doubleValue();

        assertTrue(totalUsers > 0, "Total users should be greater than 0");
        assertTrue(totalOrders >= 0, "Total orders should be 0 or greater");
        assertTrue(totalRevenue >= 0, "Total revenue should be 0 or greater");

        // Insert daily summary data to Analytics DB
        int summaryResult = analyticsJdbc.update("""
            INSERT INTO daily_summary (summary_date, total_users, total_orders, total_revenue) 
            VALUES (?::date, ?, ?, ?) 
            ON CONFLICT (summary_date) 
            DO UPDATE SET 
                total_users = EXCLUDED.total_users,
                total_orders = EXCLUDED.total_orders,
                total_revenue = EXCLUDED.total_revenue
        """, today, totalUsers, totalOrders, totalRevenue);

        assertTrue(summaryResult > 0, "Daily summary data insertion/update should succeed");

        // Verify saved summary data
        Map<String, Object> savedSummary = analyticsJdbc.queryForMap(
            "SELECT * FROM daily_summary WHERE summary_date = ?::date", today);

        assertEquals(totalUsers, ((Number) savedSummary.get("total_users")).longValue(), 
            "Saved total users should match");
        assertEquals(totalOrders, ((Number) savedSummary.get("total_orders")).longValue(), 
            "Saved total orders should match");
        assertEquals(totalRevenue, ((Number) savedSummary.get("total_revenue")).doubleValue(), 0.01, 
            "Saved total revenue should match");

        // Cache summary data in Redis
        String summaryKey = "daily_summary_" + today;
        Map<String, Object> summaryCache = Map.of(
            "date", today,
            "total_users", totalUsers,
            "total_orders", totalOrders,
            "total_revenue", totalRevenue,
            "cached_at", System.currentTimeMillis()
        );

        redisTemplate.opsForHash().putAll(summaryKey, summaryCache);
        redisTemplate.expire(summaryKey, 1, TimeUnit.DAYS);

        // Verify cached summary data
        Map<Object, Object> cachedSummary = redisTemplate.opsForHash().entries(summaryKey);
        assertEquals(today, cachedSummary.get("date"), "Cached date should match");
        assertEquals(totalUsers.toString(), cachedSummary.get("total_users").toString(), 
            "Cached user count should match");

        log.info("Daily summary data - Users: {}, Orders: {}, Revenue: ${:.2f}", 
            totalUsers, totalOrders, totalRevenue);
    }

    @Test
    @Order(6)
    @DisplayName("Container networking and isolation verification")
    void testContainerNetworkingAndIsolation() {
        // Check network information for each container
        ContainerInfo primaryInfo = containerManager.getContainer("primaryDb");
        ContainerInfo analyticsInfo = containerManager.getContainer("analyticsDb");
        ContainerInfo cacheInfo = containerManager.getContainer("cacheStore");

        // Verify network isolation
        String primaryHost = primaryInfo.container().getHost();
        Integer primaryPort = primaryInfo.container().getFirstMappedPort();
        String analyticsHost = analyticsInfo.container().getHost();
        Integer analyticsPort = analyticsInfo.container().getFirstMappedPort();
        String cacheHost = cacheInfo.container().getHost();
        Integer cachePort = cacheInfo.container().getFirstMappedPort();

        // Hosts can be the same, but ports should be different
        assertNotEquals(primaryPort, analyticsPort, "Primary and Analytics DB should use different ports");
        assertNotEquals(primaryPort, cachePort, "Primary DB and Cache should use different ports");
        assertNotEquals(analyticsPort, cachePort, "Analytics DB and Cache should use different ports");

        // Verify all container IDs are different
        String primaryId = primaryInfo.container().getContainerId();
        String analyticsId = analyticsInfo.container().getContainerId();
        String cacheId = cacheInfo.container().getContainerId();

        assertNotEquals(primaryId, analyticsId, "Primary and Analytics container IDs should be different");
        assertNotEquals(primaryId, cacheId, "Primary and Cache container IDs should be different");
        assertNotEquals(analyticsId, cacheId, "Analytics and Cache container IDs should be different");

        // Verify isolation by performing independent operations on each container
        String isolationTest = "isolation_" + System.currentTimeMillis();

        // Work with Primary DB
        primaryJdbc.update(
            "INSERT INTO users (username, email) VALUES (?, ?)",
            isolationTest + "_primary", isolationTest + "@primary.com");

        // Work with Analytics DB
        analyticsJdbc.update(
            "INSERT INTO user_analytics (user_id, event_type, event_data) VALUES (?, ?, ?::jsonb)",
            999L, "ISOLATION_TEST", "{\"test\": \"" + isolationTest + "_analytics\"}");

        // Work with Redis
        redisTemplate.opsForValue().set(isolationTest + "_cache", "cache_isolated_data");

        // Verify data independence in each store
        Integer primaryCount = primaryJdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE username = ?", 
            Integer.class, isolationTest + "_primary");
        assertEquals(1, primaryCount, "Independent data should exist in Primary DB");

        Integer analyticsCount = analyticsJdbc.queryForObject(
            "SELECT COUNT(*) FROM user_analytics WHERE event_data::text LIKE ?", 
            Integer.class, "%" + isolationTest + "_analytics%");
        assertEquals(1, analyticsCount, "Independent data should exist in Analytics DB");

        String cacheData = (String) redisTemplate.opsForValue().get(isolationTest + "_cache");
        assertEquals("cache_isolated_data", cacheData, "Independent data should exist in Cache");

        log.info("Container network isolation verified - Primary: {}:{}, Analytics: {}:{}, Cache: {}:{}",
            primaryHost, primaryPort, analyticsHost, analyticsPort, cacheHost, cachePort);
    }

    @Test
    @Order(7)
    @DisplayName("Large data processing and integrated performance test")
    void testLargeDataProcessingAndIntegratedPerformance() {
        int batchSize = 500; // Reduced for faster testing
        String testPrefix = "perf_test_";
        
        long startTime = System.currentTimeMillis();

        // 1. Create large volume user data in Primary DB
        for (int i = 0; i < batchSize; i++) {
            try {
                primaryJdbc.update(
                    "INSERT INTO users (username, email) VALUES (?, ?)",
                    testPrefix + i, testPrefix + i + "@performance.com");
                
                // Create orders every 50 users
                if (i % 50 == 0) {
                    Long userId = primaryJdbc.queryForObject(
                        "SELECT id FROM users WHERE username = ?", 
                        Long.class, testPrefix + i);
                    
                    primaryJdbc.update(
                        "INSERT INTO orders (user_id, product_name, amount, status) VALUES (?, ?, ?, ?)",
                        userId, "Performance Product " + i, 50.0 + (i % 100), "COMPLETED");
                }
            } catch (Exception e) {
                log.warn("Error creating user {} (duplicates possible): {}", i, e.getMessage());
            }
        }

        long primaryDbTime = System.currentTimeMillis() - startTime;
        
        // 2. Statistics for created data in Primary DB
        Integer actualUsers = primaryJdbc.queryForObject(
            "SELECT COUNT(*) FROM users WHERE username LIKE ?", 
            Integer.class, testPrefix + "%");
        Integer actualOrders = primaryJdbc.queryForObject(
            "SELECT COUNT(*) FROM orders WHERE product_name LIKE ?", 
            Integer.class, "Performance Product%");

        assertTrue(actualUsers > 0, "Performance test users should be created");
        log.info("Primary DB processing completed - Users: {}, Orders: {}, Time: {}ms", 
            actualUsers, actualOrders, primaryDbTime);

        // 3. Create event data in Analytics DB (batch processing)
        startTime = System.currentTimeMillis();
        
        List<Map<String, Object>> performanceUsers = primaryJdbc.queryForList(
            "SELECT id, username FROM users WHERE username LIKE ? LIMIT 50", testPrefix + "%");

        for (Map<String, Object> user : performanceUsers) {
            try {
                String eventJson = String.format("""
                    {"user_id": %s, "username": "%s", "event": "performance_test", "batch_id": "%s"}
                """, user.get("id"), user.get("username"), testPrefix);

                analyticsJdbc.update(
                    "INSERT INTO user_analytics (user_id, event_type, event_data) VALUES (?, ?, ?::jsonb)",
                    user.get("id"), "PERFORMANCE_TEST", eventJson);
            } catch (Exception e) {
                log.warn("Error creating analytics event: {}", e.getMessage());
            }
        }
        
        long analyticsDbTime = System.currentTimeMillis() - startTime;

        // 4. Create cache data in Redis (batch processing)
        startTime = System.currentTimeMillis();
        
        for (Map<String, Object> user : performanceUsers) {
            try {
                String cacheKey = "perf_user_" + user.get("id");
                Map<String, Object> userCache = Map.of(
                    "id", user.get("id"),
                    "username", user.get("username"),
                    "performance_test", true,
                    "batch_id", testPrefix
                );
                
                redisTemplate.opsForHash().putAll(cacheKey, userCache);
                redisTemplate.expire(cacheKey, 10, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Error creating Redis cache: {}", e.getMessage());
            }
        }
        
        long redisTime = System.currentTimeMillis() - startTime;

        // 5. Integrated query performance test
        startTime = System.currentTimeMillis();
        
        // Complex join query
        List<Map<String, Object>> complexResults = primaryJdbc.queryForList("""
            SELECT u.username, u.email, COUNT(o.id) as order_count, 
                   COALESCE(SUM(o.amount), 0) as total_spent
            FROM users u
            LEFT JOIN orders o ON u.id = o.user_id
            WHERE u.username LIKE ?
            GROUP BY u.id, u.username, u.email
            HAVING COUNT(o.id) > 0
            ORDER BY total_spent DESC
            LIMIT 10
        """, testPrefix + "%");

        long complexQueryTime = System.currentTimeMillis() - startTime;

        // Aggregate query in Analytics DB
        startTime = System.currentTimeMillis();
        
        Integer analyticsEvents = analyticsJdbc.queryForObject(
            "SELECT COUNT(*) FROM user_analytics WHERE event_type = 'PERFORMANCE_TEST'", 
            Integer.class);
        
        long analyticsQueryTime = System.currentTimeMillis() - startTime;

        // Performance result verification
        assertTrue(primaryDbTime < 20000, "Primary DB processing should complete within 20 seconds");
        assertTrue(analyticsDbTime < 10000, "Analytics DB processing should complete within 10 seconds");
        assertTrue(redisTime < 5000, "Redis processing should complete within 5 seconds");
        assertTrue(complexQueryTime < 5000, "Complex queries should complete within 5 seconds");
        assertTrue(analyticsQueryTime < 2000, "Analytics queries should complete within 2 seconds");

        assertFalse(complexResults.isEmpty(), "Complex query results should exist");
        assertTrue(analyticsEvents > 0, "Analytics events should be recorded");

        log.info("Large data processing performance - Primary: {}ms, Analytics: {}ms, Redis: {}ms, Complex query: {}ms", 
            primaryDbTime, analyticsDbTime, redisTime, complexQueryTime);
    }
}