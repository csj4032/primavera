package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.bean.DataSourceBeanCreator;
import com.genius.primavera.testcontainers.config.PostgreSqlContainerSpec;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * PostgreSQL 전용 DataSource 빈 생성기
 * PostgreSQL 특화 설정을 포함한 HikariDataSource를 생성합니다.
 */
@Slf4j
public class PostgreSQLBeanCreator extends DataSourceBeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        HikariConfig config = createBaseConfig(containerInfo);
        
        // PostgreSQL 특화 설정 적용
        if (containerInfo.spec() instanceof PostgreSqlContainerSpec spec) {
            applyCommonSettings(config, spec);
            applyPostgreSQLSpecificSettings(config, spec);
        } else {
            // 기본값 사용
            config.setUsername("primavera");
            config.setPassword("primavera");
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);
            log.warn("PostgreSqlContainerSpec not found for container: {}. Using default settings.", containerInfo.name());
        }
        
        log.info("Creating PostgreSQL DataSource for container: {} with pool size: {}", 
                containerInfo.name(), config.getMaximumPoolSize());
        
        return new HikariDataSource(config);
    }
    
    /**
     * PostgreSQL 특화 설정 적용
     */
    private void applyPostgreSQLSpecificSettings(HikariConfig config, PostgreSqlContainerSpec spec) {
        // PostgreSQL 연결 속성 설정
        
        // 로케일 설정
        if (spec.getLocale() != null && !spec.getLocale().isEmpty()) {
            config.addDataSourceProperty("options", "-c lc_messages=" + spec.getLocale());
        }
        
        // 인코딩 설정
        if (spec.getEncoding() != null && !spec.getEncoding().isEmpty()) {
            config.addDataSourceProperty("charSet", spec.getEncoding());
        }
        
        // SSL 모드 설정
        if (spec.getSslMode() != null) {
            config.addDataSourceProperty("sslmode", spec.getSslMode().name().toLowerCase());
        } else {
            // 기본값: SSL 비활성화 (테스트 환경)
            config.addDataSourceProperty("sslmode", "disable");
        }
        
        // 기본 스키마 설정
        config.addDataSourceProperty("currentSchema", "public");
        
        // 타임존 설정
        if (spec.getTimezone() != null && !spec.getTimezone().isEmpty()) {
            String options = config.getDataSourceProperties().getProperty("options", "");
            options += " -c timezone=" + spec.getTimezone();
            config.addDataSourceProperty("options", options.trim());
        }
        
        // 날짜 스타일 설정
        if (spec.getDateStyle() != null && !spec.getDateStyle().isEmpty()) {
            String options = config.getDataSourceProperties().getProperty("options", "");
            options += " -c datestyle='" + spec.getDateStyle() + "'";
            config.addDataSourceProperty("options", options.trim());
        }
        
        // Shared Buffers 설정
        if (spec.getSharedBuffers() != null && !spec.getSharedBuffers().isEmpty()) {
            String options = config.getDataSourceProperties().getProperty("options", "");
            options += " -c shared_buffers=" + spec.getSharedBuffers();
            config.addDataSourceProperty("options", options.trim());
        }
        
        // Work Memory 설정
        if (spec.getWorkMem() != null && !spec.getWorkMem().isEmpty()) {
            String options = config.getDataSourceProperties().getProperty("options", "");
            options += " -c work_mem=" + spec.getWorkMem();
            config.addDataSourceProperty("options", options.trim());
        }
        
        // 최대 연결 수 설정
        if (spec.getMaxConnections() != null) {
            String options = config.getDataSourceProperties().getProperty("options", "");
            options += " -c max_connections=" + spec.getMaxConnections();
            config.addDataSourceProperty("options", options.trim());
        }
        
        // PostgreSQL 성능 최적화 옵션
        config.addDataSourceProperty("prepareThreshold", "5");
        config.addDataSourceProperty("preparedStatementCacheQueries", "256");
        config.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");
        config.addDataSourceProperty("reWriteBatchedInserts", "true");
        
        // TCP 설정
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("socketTimeout", "30");
        
        log.debug("Applied PostgreSQL specific settings - Timezone: {}, SSLMode: {}", 
                spec.getTimezone(), spec.getSslMode());
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.POSTGRESQL;
    }
}