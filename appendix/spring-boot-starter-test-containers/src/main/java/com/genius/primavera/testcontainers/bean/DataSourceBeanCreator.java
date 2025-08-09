package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.DatabaseContainerSpec;
import com.genius.primavera.testcontainers.config.MariaDbContainerSpec;
import com.genius.primavera.testcontainers.config.MySqlContainerSpec;
import com.genius.primavera.testcontainers.config.PostgreSqlContainerSpec;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public abstract class DataSourceBeanCreator implements BeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(containerInfo.getJdbcUrl());
        config.setDriverClassName(containerInfo.type().getDriverClassName());
        
        // 타입별로 사용자명과 비밀번호 추출
        String username = "primavera";
        String password = "primavera";
        Integer maxConnections = 10;
        Integer connectionTimeout = 30000;
        
        if (containerInfo.spec() instanceof MariaDbContainerSpec mariaDbSpec) {
            username = mariaDbSpec.getUsername();
            password = mariaDbSpec.getPassword();
            maxConnections = mariaDbSpec.getMaxConnections() != null ? mariaDbSpec.getMaxConnections() : 10;
            connectionTimeout = mariaDbSpec.getConnectionTimeout() != null ? mariaDbSpec.getConnectionTimeout() : 30000;
        } else if (containerInfo.spec() instanceof MySqlContainerSpec mysqlSpec) {
            username = mysqlSpec.getUsername();
            password = mysqlSpec.getPassword();
            maxConnections = mysqlSpec.getMaxConnections() != null ? mysqlSpec.getMaxConnections() : 10;
            connectionTimeout = mysqlSpec.getConnectionTimeout() != null ? mysqlSpec.getConnectionTimeout() : 30000;
        } else if (containerInfo.spec() instanceof PostgreSqlContainerSpec pgSpec) {
            username = pgSpec.getUsername();
            password = pgSpec.getPassword();
            maxConnections = pgSpec.getMaxConnections() != null ? pgSpec.getMaxConnections() : 10;
            connectionTimeout = pgSpec.getConnectionTimeout() != null ? pgSpec.getConnectionTimeout() : 30000;
        } else if (containerInfo.spec() instanceof DatabaseContainerSpec dbSpec) {
            username = dbSpec.getUsername();
            password = dbSpec.getPassword();
            maxConnections = dbSpec.getMaxConnections() != null ? dbSpec.getMaxConnections() : 10;
            connectionTimeout = dbSpec.getConnectionTimeout() != null ? dbSpec.getConnectionTimeout() : 30000;
        }
        
        config.setUsername(username);
        config.setPassword(password);
        
        config.setPoolName(containerInfo.name() + "-pool");
        config.setMaximumPoolSize(maxConnections);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(connectionTimeout);
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