package com.genius.primavera.testcontainer.v4;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.CONCURRENT)
@DisplayName("Thread Safety and Concurrent Access Tests")
@EnableTestContainers({
    @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "concurrentDb"),
    @EnableTestContainers.TestContainer(type = ContainerType.REDIS, name = "concurrentCache")
})
class V4ThreadSafetyTest {
    
    @Autowired
    @Qualifier("concurrentDb")
    private DataSource concurrentDataSource;
    
    @Autowired
    @Qualifier("concurrentCache")
    private RedisTemplate<String, Object> concurrentRedisTemplate;
    
    private static final int THREAD_COUNT = 10;
    private static final int OPERATIONS_PER_THREAD = 50;
    
    @Test
    @Order(1)
    @DisplayName("ContainerRegistry thread safety validation")
    void testContainerRegistryThreadSafety() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        List<ContainerManager> managers = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    ContainerManager manager = ContainerRegistry.get();
                    if (manager != null) {
                        managers.add(manager);
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(30, TimeUnit.SECONDS), "All threads should complete within 30 seconds");
        assertEquals(THREAD_COUNT, successCount.get(), "All threads should successfully get ContainerManager");
        
        if (!managers.isEmpty()) {
            ContainerManager firstManager = managers.get(0);
            assertTrue(managers.stream().allMatch(manager -> manager == firstManager),
                "All threads should get the same ContainerManager instance");
        }
        
        executor.shutdown();
        log.info("ContainerRegistry thread safety validated with {} threads", THREAD_COUNT);
    }
    
    @Test
    @Order(2)
    @DisplayName("Database connection pool concurrent access")
    void testDatabaseConnectionPoolConcurrency() throws InterruptedException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(concurrentDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS concurrent_test (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "thread_name VARCHAR(100), " +
            "operation_number INT, " +
            "timestamp BIGINT, " +
            "INDEX idx_thread (thread_name)" +
            ")");
        
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger totalOperations = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        for (int threadIndex = 0; threadIndex < THREAD_COUNT; threadIndex++) {
            final int threadNum = threadIndex;
            executor.submit(() -> {
                try {
                    String threadName = "thread-" + threadNum;
                    for (int op = 0; op < OPERATIONS_PER_THREAD; op++) {
                        try {
                            jdbcTemplate.update(
                                "INSERT INTO concurrent_test (thread_name, operation_number, timestamp) VALUES (?, ?, ?)",
                                threadName, op, System.nanoTime());
                            totalOperations.incrementAndGet();
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            log.error("Database operation failed in {}: {}", threadName, e.getMessage());
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(60, TimeUnit.SECONDS), "All database operations should complete");
        
        Integer recordCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM concurrent_test", Integer.class);
        
        assertEquals(0, errorCount.get(), "No database errors should occur during concurrent access");
        assertEquals(THREAD_COUNT * OPERATIONS_PER_THREAD, totalOperations.get(),
            "All operations should complete successfully");
        assertEquals(totalOperations.get(), recordCount.intValue(),
            "All records should be persisted correctly");
        
        executor.shutdown();
        log.info("Database concurrent access validated: {} operations, {} records, {} errors",
            totalOperations.get(), recordCount, errorCount.get());
    }
    
    @Test
    @Order(3)
    @DisplayName("Redis concurrent operations validation")
    void testRedisConcurrentOperations() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        
        String baseKey = "concurrent-test-";
        
        for (int threadIndex = 0; threadIndex < THREAD_COUNT; threadIndex++) {
            final int threadNum = threadIndex;
            executor.submit(() -> {
                try {
                    String threadName = "thread-" + threadNum;
                    
                    for (int op = 0; op < OPERATIONS_PER_THREAD; op++) {
                        String key = baseKey + threadNum + "-" + op;
                        String value = threadName + "-value-" + op + "-" + System.nanoTime();
                        
                        try {
                            concurrentRedisTemplate.opsForValue().set(key, value);
                            Object retrieved = concurrentRedisTemplate.opsForValue().get(key);
                            
                            if (value.equals(retrieved)) {
                                successCount.incrementAndGet();
                            } else {
                                conflictCount.incrementAndGet();
                                log.warn("Value mismatch in {}: expected={}, actual={}", threadName, value, retrieved);
                            }
                        } catch (Exception e) {
                            conflictCount.incrementAndGet();
                            log.error("Redis operation failed in {}: {}", threadName, e.getMessage());
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(60, TimeUnit.SECONDS), "All Redis operations should complete");
        
        int expectedOperations = THREAD_COUNT * OPERATIONS_PER_THREAD;
        assertEquals(expectedOperations, successCount.get() + conflictCount.get(),
            "All operations should be accounted for");
        
        assertTrue(successCount.get() > expectedOperations * 0.95,
            String.format("At least 95%% of operations should succeed, got %.2f%%",
                (double) successCount.get() / expectedOperations * 100));
        
        executor.shutdown();
        log.info("Redis concurrent operations validated: {} success, {} conflicts out of {} total",
            successCount.get(), conflictCount.get(), expectedOperations);
    }
    
    @Test
    @Order(4)
    @DisplayName("Container manager state consistency under load")
    void testContainerManagerStateConsistency() throws InterruptedException {
        ContainerManager manager = ContainerRegistry.get();
        
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger consistentStateCount = new AtomicInteger(0);
        AtomicInteger inconsistentStateCount = new AtomicInteger(0);
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    for (int op = 0; op < OPERATIONS_PER_THREAD; op++) {
                        boolean isStarted = manager.isStarted();
                        ContainerInfo dbInfo = manager.getContainer("concurrentDb");
                        ContainerInfo cacheInfo = manager.getContainer("concurrentCache");
                        
                        if (isStarted && dbInfo != null && cacheInfo != null &&
                            dbInfo.getContainer().isRunning() && cacheInfo.getContainer().isRunning()) {
                            consistentStateCount.incrementAndGet();
                        } else {
                            inconsistentStateCount.incrementAndGet();
                            log.warn("Inconsistent state detected: started={}, dbInfo={}, cacheInfo={}",
                                isStarted, dbInfo != null, cacheInfo != null);
                        }
                        
                        Thread.sleep(1);
                    }
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(60, TimeUnit.SECONDS), "All state checks should complete");
        
        int totalChecks = consistentStateCount.get() + inconsistentStateCount.get();
        assertEquals(THREAD_COUNT * OPERATIONS_PER_THREAD, totalChecks,
            "All state checks should be accounted for");
        assertEquals(0, inconsistentStateCount.get(),
            "Container manager state should be consistent under concurrent access");
        
        executor.shutdown();
        log.info("Container manager state consistency validated: {} consistent, {} inconsistent checks",
            consistentStateCount.get(), inconsistentStateCount.get());
    }
    
    @Test
    @Order(5)
    @DisplayName("Cross-container transaction isolation")
    void testCrossContainerTransactionIsolation() throws InterruptedException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(concurrentDataSource);
        
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS transaction_test (" +
            "id INT PRIMARY KEY, " +
            "balance DECIMAL(10,2), " +
            "version INT DEFAULT 0" +
            ")");
        
        jdbcTemplate.update("INSERT INTO transaction_test (id, balance) VALUES (1, 1000.00) " +
            "ON DUPLICATE KEY UPDATE balance = VALUES(balance)");
        
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicLong totalDeducted = new AtomicLong(0);
        AtomicInteger successfulTransactions = new AtomicInteger(0);
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    String cacheKey = "transaction-lock-" + threadIndex;
                    String transactionId = "tx-" + threadIndex + "-" + System.nanoTime();
                    
                    for (int op = 0; op < 5; op++) {
                        try {
                            Boolean lockAcquired = concurrentRedisTemplate.opsForValue()
                                .setIfAbsent(cacheKey, transactionId, Duration.ofSeconds(1));
                            
                            if (Boolean.TRUE.equals(lockAcquired)) {
                                try {
                                    Double currentBalance = jdbcTemplate.queryForObject(
                                        "SELECT balance FROM transaction_test WHERE id = 1", Double.class);
                                    
                                    if (currentBalance != null && currentBalance >= 10.0) {
                                        jdbcTemplate.update(
                                            "UPDATE transaction_test SET balance = balance - 10.0 WHERE id = 1");
                                        totalDeducted.addAndGet(1000);
                                        successfulTransactions.incrementAndGet();
                                    }
                                } finally {
                                    concurrentRedisTemplate.delete(cacheKey);
                                }
                            }
                            
                            Thread.sleep(10);
                        } catch (Exception e) {
                            log.debug("Transaction attempt failed: {}", e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        assertTrue(latch.await(60, TimeUnit.SECONDS), "All transactions should complete");
        
        Double finalBalance = jdbcTemplate.queryForObject(
            "SELECT balance FROM transaction_test WHERE id = 1", Double.class);
        
        assertNotNull(finalBalance, "Final balance should not be null");
        
        double expectedBalance = 1000.0 - (totalDeducted.get() / 100.0);
        assertEquals(expectedBalance, finalBalance, 0.01,
            String.format("Balance should be %.2f but was %.2f", expectedBalance, finalBalance));
        
        executor.shutdown();
        log.info("Transaction isolation validated: {} successful transactions, final balance: {}",
            successfulTransactions.get(), finalBalance);
    }
}