package com.genius.primavera.infrastructure;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Vault에서 관리되는 민감정보를 사용한 안전한 DataSource 설정.
 * 기존의 평문 설정을 대체하여 보안을 강화합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("vault")
public class SecureDataSourceConfiguration {

    private final VaultConfiguration.DatabaseProperties databaseProperties;

    @Bean
    @Primary
    public DataSource secureDataSource() {
        log.info("Vault 기반 보안 DataSource 설정 시작");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseProperties.getUrl());
        config.setUsername(databaseProperties.getUsername());
        config.setPassword(databaseProperties.getPassword());
        config.setDriverClassName(databaseProperties.getDriverClassName());

        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // 연결 검증 설정
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(3000);

        HikariDataSource dataSource = new HikariDataSource(config);

        log.info("Vault 기반 보안 DataSource 설정 완료 - URL: {}, Username: {}",
                maskUrl(databaseProperties.getUrl()),
                maskUsername(databaseProperties.getUsername()));

        return dataSource;
    }

    /**
     * URL에서 민감정보를 마스킹하여 로그에 안전하게 출력
     */
    private String maskUrl(String url) {
        if (url == null) return "null";
        return url.replaceAll("(password=)[^&]*", "$1***");
    }

    /**
     * 사용자명을 부분적으로 마스킹하여 로그에 안전하게 출력
     */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) return "***";
        return username.substring(0, 2) + "***";
    }
}