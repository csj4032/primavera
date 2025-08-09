package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.bean.DataSourceBeanCreator;
import com.genius.primavera.testcontainers.config.MySqlContainerSpec;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * MySQL 전용 DataSource 빈 생성기
 * MySQL 특화 설정을 포함한 HikariDataSource를 생성합니다.
 */
@Slf4j
public class MySQLBeanCreator extends DataSourceBeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        HikariConfig config = createBaseConfig(containerInfo);
        
        // MySQL 특화 설정 적용
        if (containerInfo.spec() instanceof MySqlContainerSpec spec) {
            applyCommonSettings(config, spec);
            applyMySQLSpecificSettings(config, spec);
        } else {
            // 기본값 사용
            config.setUsername("primavera");
            config.setPassword("primavera");
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);
            log.warn("MySqlContainerSpec not found for container: {}. Using default settings.", containerInfo.name());
        }
        
        log.info("Creating MySQL DataSource for container: {} with pool size: {}", 
                containerInfo.name(), config.getMaximumPoolSize());
        
        return new HikariDataSource(config);
    }
    
    /**
     * MySQL 특화 설정 적용
     */
    private void applyMySQLSpecificSettings(HikariConfig config, MySqlContainerSpec spec) {
        // 문자셋 설정
        if (spec.getCharacterSet() != null && !spec.getCharacterSet().isEmpty()) {
            config.addDataSourceProperty("characterEncoding", spec.getCharacterSet());
            config.addDataSourceProperty("useUnicode", "true");
        }
        
        // Collation 설정
        if (spec.getCollation() != null && !spec.getCollation().isEmpty()) {
            config.addDataSourceProperty("connectionCollation", spec.getCollation());
        }
        
        // 타임존 설정
        if (spec.getDefaultTimeZone() != null && !spec.getDefaultTimeZone().isEmpty()) {
            config.addDataSourceProperty("serverTimezone", spec.getDefaultTimeZone());
        } else {
            config.addDataSourceProperty("serverTimezone", "Asia/Seoul");
        }
        
        // SQL Mode 설정
        if (spec.getSqlMode() != null) {
            config.addDataSourceProperty("sessionVariables", "sql_mode='" + spec.getSqlMode().name() + "'");
        }
        
        // Storage Engine 설정
        if (spec.getDefaultStorageEngine() != null) {
            config.addDataSourceProperty("sessionVariables", 
                    config.getDataSourceProperties().getProperty("sessionVariables", "") + 
                    ",default_storage_engine=" + spec.getDefaultStorageEngine().name());
        }
        
        // MySQL 성능 최적화 옵션 (기본값 사용)
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "256");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useLocalTransactionState", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // Batch 처리 최적화
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("allowMultiQueries", "true");
        
        // 연결 설정
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("tcpNoDelay", "true");
        
        // SSL 설정 (기본값: 테스트 환경에서는 비활성화)
        if (!spec.getSslEnabled()) {
            config.addDataSourceProperty("useSSL", "false");
            config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
        }
        
        log.debug("Applied MySQL specific settings - Timezone: {}, SqlMode: {}, StorageEngine: {}", 
                spec.getDefaultTimeZone(), spec.getSqlMode(), spec.getDefaultStorageEngine());
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MYSQL;
    }
}