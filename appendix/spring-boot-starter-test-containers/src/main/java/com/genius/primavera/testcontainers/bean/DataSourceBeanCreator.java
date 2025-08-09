package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.DatabaseContainerSpec;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

/**
 * DataSource 빈 생성을 위한 추상 클래스
 * 각 데이터베이스 타입별로 구체적인 구현체를 제공합니다.
 */
@Slf4j
public abstract class DataSourceBeanCreator implements BeanCreator {
    
    /**
     * 모든 DataSource에 공통적으로 적용되는 기본 HikariConfig 생성
     */
    protected HikariConfig createBaseConfig(ContainerInfo containerInfo) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(containerInfo.getJdbcUrl());
        config.setDriverClassName(containerInfo.type().getDriverClassName());
        config.setPoolName(containerInfo.name() + "-pool");
        
        // 기본값 설정
        config.setMinimumIdle(2);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        
        log.debug("Created base HikariConfig for container: {} with URL: {}", 
                containerInfo.name(), containerInfo.getJdbcUrl());
        
        return config;
    }
    
    /**
     * DatabaseContainerSpec에서 공통 설정 추출
     */
    protected void applyCommonSettings(HikariConfig config, DatabaseContainerSpec spec) {
        config.setUsername(spec.getUsername());
        config.setPassword(spec.getPassword());
        config.setMaximumPoolSize(spec.getMaxConnections() != null ? spec.getMaxConnections() : 10);
        config.setConnectionTimeout(spec.getConnectionTimeout() != null ? spec.getConnectionTimeout() : 30000);
    }
    
    // 내부 클래스는 제거되고 독립적인 클래스로 이동됩니다.
    // @Deprecated 표시로 하위 호환성 유지
    @Deprecated(since = "2.0", forRemoval = true)
    public static class MariaDBBeanCreator extends DataSourceBeanCreator {
        @Override
        public ContainerType getSupportedType() {
            return ContainerType.MARIADB;
        }
        
        @Override
        public Object createBean(ContainerInfo containerInfo) {
            log.warn("Using deprecated MariaDBBeanCreator. Please use com.genius.primavera.testcontainers.bean.datasource.MariaDBBeanCreator instead");
            return new com.genius.primavera.testcontainers.bean.datasource.MariaDBBeanCreator().createBean(containerInfo);
        }
    }
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static class MySQLBeanCreator extends DataSourceBeanCreator {
        @Override
        public ContainerType getSupportedType() {
            return ContainerType.MYSQL;
        }
        
        @Override
        public Object createBean(ContainerInfo containerInfo) {
            log.warn("Using deprecated MySQLBeanCreator. Please use com.genius.primavera.testcontainers.bean.datasource.MySQLBeanCreator instead");
            return new com.genius.primavera.testcontainers.bean.datasource.MySQLBeanCreator().createBean(containerInfo);
        }
    }
    
    @Deprecated(since = "2.0", forRemoval = true)
    public static class PostgreSQLBeanCreator extends DataSourceBeanCreator {
        @Override
        public ContainerType getSupportedType() {
            return ContainerType.POSTGRESQL;
        }
        
        @Override
        public Object createBean(ContainerInfo containerInfo) {
            log.warn("Using deprecated PostgreSQLBeanCreator. Please use com.genius.primavera.testcontainers.bean.datasource.PostgreSQLBeanCreator instead");
            return new com.genius.primavera.testcontainers.bean.datasource.PostgreSQLBeanCreator().createBean(containerInfo);
        }
    }
}