package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.DatabaseContainerSpec;
import com.genius.primavera.testcontainers.config.PostgreSqlContainerSpec;
import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class PostgreSQLContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        String image = spec.getImage() != null ? spec.getImage() : ContainerType.POSTGRESQL.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;
        
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(image))
                .withStartupTimeout(Duration.ofSeconds(timeout));
        
        if (spec instanceof PostgreSqlContainerSpec pgSpec) {
            container.withDatabaseName(pgSpec.getDatabase())
                    .withUsername(pgSpec.getUsername())
                    .withPassword(pgSpec.getPassword());
                    
            if (pgSpec.getLocale() != null) {
                container.withEnv("LANG", pgSpec.getLocale())
                        .withEnv("LC_ALL", pgSpec.getLocale());
            }
            if (pgSpec.getEncoding() != null) {
                container.withEnv("POSTGRES_ENCODING", pgSpec.getEncoding());
            }
            if (pgSpec.getSharedBuffers() != null) {
                container.withCommand("postgres", "-c", "shared_buffers=" + pgSpec.getSharedBuffers());
            }
            if (pgSpec.getWorkMem() != null) {
                container.withEnv("POSTGRES_WORK_MEM", pgSpec.getWorkMem());
            }
            if (pgSpec.getMaintenanceWorkMem() != null) {
                container.withEnv("POSTGRES_MAINTENANCE_WORK_MEM", pgSpec.getMaintenanceWorkMem());
            }
            if (pgSpec.getTimezone() != null) {
                container.withEnv("TZ", pgSpec.getTimezone())
                        .withEnv("PGTZ", pgSpec.getTimezone());
            }
            if (pgSpec.getMaxConnections() != null) {
                container.withEnv("POSTGRES_MAX_CONNECTIONS", pgSpec.getMaxConnections().toString());
            }
            
            if (pgSpec.getInitScript() != null) {
                String scriptPath = pgSpec.getInitScript();
                if (scriptPath.startsWith("classpath:")) {
                    scriptPath = scriptPath.substring("classpath:".length());
                }
                container.withInitScript(scriptPath);
            }
        } else if (spec instanceof DatabaseContainerSpec dbSpec) {
            container.withDatabaseName(dbSpec.getDatabase())
                    .withUsername(dbSpec.getUsername())
                    .withPassword(dbSpec.getPassword());
            
            if (dbSpec.getInitScript() != null) {
                String scriptPath = dbSpec.getInitScript();
                if (scriptPath.startsWith("classpath:")) {
                    scriptPath = scriptPath.substring("classpath:".length());
                }
                container.withInitScript(scriptPath);
            }
        } else {
            container.withDatabaseName("primavera")
                    .withUsername("primavera")
                    .withPassword("primavera");
        }
        
        if (spec.getEnvironment() != null) {
            spec.getEnvironment().forEach(container::withEnv);
        }
        
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.POSTGRESQL;
    }
}