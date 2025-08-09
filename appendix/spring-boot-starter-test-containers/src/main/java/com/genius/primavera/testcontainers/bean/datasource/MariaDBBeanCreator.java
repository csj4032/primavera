package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.bean.DataSourceBeanCreator;
import com.genius.primavera.testcontainers.config.MariaDbContainerSpec;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * MariaDB 전용 DataSource 빈 생성기
 * MariaDB 특화 설정을 포함한 HikariDataSource를 생성합니다.
 */
@Slf4j
public class MariaDBBeanCreator extends DataSourceBeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        HikariConfig config = createBaseConfig(containerInfo);
        
        // MariaDB 특화 설정 적용
        if (containerInfo.spec() instanceof MariaDbContainerSpec spec) {
            applyCommonSettings(config, spec);
            applyMariaDBSpecificSettings(config, spec);
        } else {
            // 기본값 사용
            config.setUsername("primavera");
            config.setPassword("primavera");
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);
            log.warn("MariaDbContainerSpec not found for container: {}. Using default settings.", containerInfo.name());
        }
        
        log.info("Creating MariaDB DataSource for container: {} with pool size: {}", 
                containerInfo.name(), config.getMaximumPoolSize());
        
        return new HikariDataSource(config);
    }
    
    /**
     * MariaDB 특화 설정 적용
     */
    private void applyMariaDBSpecificSettings(HikariConfig config, MariaDbContainerSpec spec) {
        // 문자셋 설정
        if (spec.getCharacterSet() != null && !spec.getCharacterSet().isEmpty()) {
            config.addDataSourceProperty("characterEncoding", spec.getCharacterSet());
        }
        
        // Collation 설정
        if (spec.getCollation() != null && !spec.getCollation().isEmpty()) {
            config.addDataSourceProperty("connectionCollation", spec.getCollation());
        }
        
        // MariaDB 최적화 옵션
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "256");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("tcpNoDelay", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        
        log.debug("Applied MariaDB specific settings - Charset: {}, Collation: {}", 
                spec.getCharacterSet(), spec.getCollation());
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MARIADB;
    }
}