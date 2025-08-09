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
        
        // PostgreSQL options 파라미터 조합 (런타임 변경 가능한 설정만)
        StringBuilder options = new StringBuilder();
        
        // 로케일 설정 (런타임 변경 불가 - 제거)
        // 타임존 설정 (런타임 변경 가능)
        if (spec.getTimezone() != null && !spec.getTimezone().isEmpty()) {
            options.append("-c timezone=").append(spec.getTimezone());
        }
        
        // 날짜 스타일 설정 (런타임 변경 가능)
        if (spec.getDateStyle() != null && !spec.getDateStyle().isEmpty()) {
            if (options.length() > 0) {
                options.append(" ");
            }
            options.append("-c datestyle=").append(spec.getDateStyle());
        }
        
        // 다음 설정들은 서버 재시작 없이 변경할 수 없으므로 제거:
        // - shared_buffers (서버 시작 시에만 설정 가능)
        // - work_mem (세션별 설정 가능하지만 connection string에서는 권장하지 않음)
        // - maintenance_work_mem (서버 시작 시에만 설정 가능)  
        // - wal_buffers (서버 시작 시에만 설정 가능)
        // - max_connections (서버 시작 시에만 설정 가능)
        
        // options 한번에 설정
        if (options.length() > 0) {
            config.addDataSourceProperty("options", options.toString());
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