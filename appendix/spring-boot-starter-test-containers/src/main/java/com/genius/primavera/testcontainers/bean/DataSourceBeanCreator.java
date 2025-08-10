package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.DatabaseContainerSpec;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

@Slf4j
public abstract class DataSourceBeanCreator implements BeanCreator {
    
    protected HikariConfig createBaseConfig(ContainerInfo containerInfo) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(containerInfo.getJdbcUrl());
        config.setDriverClassName(containerInfo.type().getDriverClassName());
        config.setPoolName(containerInfo.name() + "-pool");
        
        config.setMinimumIdle(2);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        
        log.debug("Created base HikariConfig for container: {} with URL: {}", 
                containerInfo.name(), containerInfo.getJdbcUrl());
        
        return config;
    }
    
    protected void applyCommonSettings(HikariConfig config, DatabaseContainerSpec spec) {
        config.setUsername(spec.getUsername());
        config.setPassword(spec.getPassword());
        config.setMaximumPoolSize(spec.getMaxConnections() != null ? spec.getMaxConnections() : 10);
        config.setConnectionTimeout(spec.getConnectionTimeout() != null ? spec.getConnectionTimeout() : 30000);
    }
    
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