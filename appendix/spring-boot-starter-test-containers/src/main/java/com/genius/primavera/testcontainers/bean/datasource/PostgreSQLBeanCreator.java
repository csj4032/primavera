package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.bean.DataSourceBeanCreator;
import com.genius.primavera.testcontainers.config.PostgreSqlContainerSpec;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostgreSQLBeanCreator extends DataSourceBeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        HikariConfig config = createBaseConfig(containerInfo);
        
        if (containerInfo.spec() instanceof PostgreSqlContainerSpec spec) {
            applyCommonSettings(config, spec);
            applyPostgreSQLSpecificSettings(config, spec);
        } else {
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
    
    private void applyPostgreSQLSpecificSettings(HikariConfig config, PostgreSqlContainerSpec spec) {
        
        if (spec.getEncoding() != null && !spec.getEncoding().isEmpty()) {
            config.addDataSourceProperty("charSet", spec.getEncoding());
        }
        
        if (spec.getSslMode() != null) {
            config.addDataSourceProperty("sslmode", spec.getSslMode().name().toLowerCase());
        } else {
            config.addDataSourceProperty("sslmode", "disable");
        }
        
        config.addDataSourceProperty("currentSchema", "public");
        
        StringBuilder options = new StringBuilder();
        
        if (spec.getTimezone() != null && !spec.getTimezone().isEmpty()) {
            options.append("-c timezone=").append(spec.getTimezone());
        }
        
        if (spec.getDateStyle() != null && !spec.getDateStyle().isEmpty()) {
            if (options.length() > 0) {
                options.append(" ");
            }
            options.append("-c datestyle=").append(spec.getDateStyle());
        }
        
        
        if (options.length() > 0) {
            config.addDataSourceProperty("options", options.toString());
        }
        
        config.addDataSourceProperty("prepareThreshold", "5");
        config.addDataSourceProperty("preparedStatementCacheQueries", "256");
        config.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");
        config.addDataSourceProperty("reWriteBatchedInserts", "true");
        
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