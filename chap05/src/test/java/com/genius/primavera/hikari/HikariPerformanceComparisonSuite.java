package com.genius.primavera.hikari;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
@DisplayName("HikariCP connection test connection")
public class HikariPerformanceComparisonSuite {

    @Test
    @DisplayName("HikariCP test test connection")
    void performanceComparisonGuide() {
        log.info("=== HikariCP test test connection ===");
        log.info("");
        log.info(" test connection test connection:");
        log.info("");
        log.info("1.  Minimal Pool (test)");
        log.info("   - Pool Size: 1~3 connections");
        log.info("   - test: connection test, connection test");
        log.info("   - test: file test test");
        log.info("");
        log.info("2. 🟢 Balanced Pool (test)");
        log.info("   - Pool Size: 5~10 connections");
        log.info("   - test: test file test");
        log.info("   - test: file test");
        log.info("");
        log.info("3. 🟠 Performance Pool (test)");
        log.info("   - Pool Size: 10~20 connections");
        log.info("   - test: test processing, test connection test");
        log.info("   - test: test test");
        log.info("");
        log.info("4.  Resource-Constrained Pool (connection test)");
        log.info("   - Pool Size: 2~5 connections");
        log.info("   - test: test connection, connection test");
        log.info("   - test: Endpoint file test");
        log.info("");
        log.info(" should test processing connection test connection test!");
        log.info(" file processing(queries/sec), file, should connection should5!");
        log.info("");
    }
}