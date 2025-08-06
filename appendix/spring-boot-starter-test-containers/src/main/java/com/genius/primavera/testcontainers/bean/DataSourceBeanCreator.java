package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public abstract class DataSourceBeanCreator implements BeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(containerInfo.getJdbcUrl());
        config.setDriverClassName(containerInfo.type().getDriverClassName());
        config.setUsername(containerInfo.spec().getUsernameOrDefault());
        config.setPassword(containerInfo.spec().getPasswordOrDefault());
        
        config.setPoolName(containerInfo.name() + "-pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        
        return new HikariDataSource(config);
    }
    
    public static class MariaDBBeanCreator extends DataSourceBeanCreator {
        @Override
        public ContainerType getSupportedType() {
            return ContainerType.MARIADB;
        }
    }
    
    public static class MySQLBeanCreator extends DataSourceBeanCreator {
        @Override
        public ContainerType getSupportedType() {
            return ContainerType.MYSQL;
        }
    }
    
    public static class PostgreSQLBeanCreator extends DataSourceBeanCreator {
        @Override
        public ContainerType getSupportedType() {
            return ContainerType.POSTGRESQL;
        }
    }
}