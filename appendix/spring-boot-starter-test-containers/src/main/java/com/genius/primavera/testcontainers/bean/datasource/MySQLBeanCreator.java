package com.genius.primavera.testcontainers.bean.datasource;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.bean.DataSourceBeanCreator;
import com.genius.primavera.testcontainers.config.MySqlContainerSpec;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MySQLBeanCreator extends DataSourceBeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        HikariConfig config = createBaseConfig(containerInfo);
        
        if (containerInfo.spec() instanceof MySqlContainerSpec spec) {
            applyCommonSettings(config, spec);
            applyMySQLSpecificSettings(config, spec);
        } else {
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
    
    private void applyMySQLSpecificSettings(HikariConfig config, MySqlContainerSpec spec) {
        if (spec.getCharacterSet() != null && !spec.getCharacterSet().isEmpty()) {
            config.addDataSourceProperty("characterEncoding", spec.getCharacterSet());
            config.addDataSourceProperty("useUnicode", "true");
        }
        
        if (spec.getCollation() != null && !spec.getCollation().isEmpty()) {
            config.addDataSourceProperty("connectionCollation", spec.getCollation());
        }
        
        if (spec.getDefaultTimeZone() != null && !spec.getDefaultTimeZone().isEmpty()) {
            config.addDataSourceProperty("serverTimezone", spec.getDefaultTimeZone());
        } else {
            config.addDataSourceProperty("serverTimezone", "Asia/Seoul");
        }
        
        StringBuilder sessionVars = new StringBuilder();
        
        if (spec.getSqlMode() != null) {
            sessionVars.append("sql_mode='").append(spec.getSqlMode().name()).append("'");
        }
        
        if (spec.getDefaultStorageEngine() != null) {
            if (sessionVars.length() > 0) {
                sessionVars.append(",");
            }
            sessionVars.append("default_storage_engine=").append(spec.getDefaultStorageEngine().name());
        }
        
        if (sessionVars.length() > 0) {
            config.addDataSourceProperty("sessionVariables", sessionVars.toString());
        }
        
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
        
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("allowMultiQueries", "true");
        
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("tcpKeepAlive", "true");
        config.addDataSourceProperty("tcpNoDelay", "true");
        
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