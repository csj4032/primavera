package com.genius.primavera.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
@EnableTestContainers({
        @EnableTestContainers.TestContainer(type = ContainerType.MARIADB, name = "parallelDb")
})
public class ParallelExecutionTest {

    @Autowired
    private DataSource parallelDb;

    @Test
    void testParallelExecution1() throws InterruptedException {
        assertNotNull(parallelDb, "DataSource should be injected");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(parallelDb);
        jdbcTemplate.update("CREATE TABLE IF NOT EXISTS parallel_test1 (id INT AUTO_INCREMENT PRIMARY KEY, data VARCHAR(50))");
        jdbcTemplate.update("INSERT INTO parallel_test1 (data) VALUES (?)", "test1-data-" + Thread.currentThread().getName());

        Thread.sleep(100);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM parallel_test1", Integer.class);
        assertTrue(count >= 1, "Should have at least 1 record");

        log.info(" Parallel execution test 1 completed on thread: {}", Thread.currentThread().getName());
    }

    @Test
    void testParallelExecution2() throws InterruptedException {
        assertNotNull(parallelDb, "DataSource should be injected");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(parallelDb);
        jdbcTemplate.update("CREATE TABLE IF NOT EXISTS parallel_test2 (id INT AUTO_INCREMENT PRIMARY KEY, data VARCHAR(50))");
        jdbcTemplate.update("INSERT INTO parallel_test2 (data) VALUES (?)", "test2-data-" + Thread.currentThread().getName());

        Thread.sleep(150);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM parallel_test2", Integer.class);
        assertTrue(count >= 1, "Should have at least 1 record");

        log.info(" Parallel execution test 2 completed on thread: {}", Thread.currentThread().getName());
    }

    @Test
    void testParallelExecution3() throws InterruptedException {
        assertNotNull(parallelDb, "DataSource should be injected");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(parallelDb);
        jdbcTemplate.update("CREATE TABLE IF NOT EXISTS parallel_test3 (id INT AUTO_INCREMENT PRIMARY KEY, data VARCHAR(50))");
        jdbcTemplate.update("INSERT INTO parallel_test3 (data) VALUES (?)", "test3-data-" + Thread.currentThread().getName());

        Thread.sleep(200);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM parallel_test3", Integer.class);
        assertTrue(count >= 1, "Should have at least 1 record");

        log.info(" Parallel execution test 3 completed on thread: {}", Thread.currentThread().getName());
    }
}