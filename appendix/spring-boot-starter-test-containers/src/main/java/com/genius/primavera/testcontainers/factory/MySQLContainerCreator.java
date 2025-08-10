package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.DatabaseContainerSpec;
import com.genius.primavera.testcontainers.config.MySqlContainerSpec;
import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class MySQLContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        String image = spec.getImage() != null ? spec.getImage() : ContainerType.MYSQL.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;
        
        MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse(image))
                .withStartupTimeout(Duration.ofSeconds(timeout));
        
        if (spec instanceof MySqlContainerSpec mysqlSpec) {
            container.withDatabaseName(mysqlSpec.getDatabase())
                    .withUsername(mysqlSpec.getUsername())
                    .withPassword(mysqlSpec.getPassword())
                    .withEnv("MYSQL_ROOT_PASSWORD", mysqlSpec.getRootPassword());
                    
            if (mysqlSpec.getCharacterSet() != null) {
                container.withEnv("MYSQL_CHARACTER_SET_SERVER", mysqlSpec.getCharacterSet());
            }
            if (mysqlSpec.getCollation() != null) {
                container.withEnv("MYSQL_COLLATION_SERVER", mysqlSpec.getCollation());
            }
            if (mysqlSpec.getDefaultTimeZone() != null) {
                container.withEnv("MYSQL_DEFAULT_TIME_ZONE", mysqlSpec.getDefaultTimeZone());
            }
            if (mysqlSpec.getSqlMode() != null) {
                container.withEnv("MYSQL_SQL_MODE", mysqlSpec.getSqlMode().name());
            }
            if (mysqlSpec.getBinlogEnabled() != null && mysqlSpec.getBinlogEnabled()) {
                container.withEnv("MYSQL_LOG_BIN", "1");
            }
        } else if (spec instanceof DatabaseContainerSpec dbSpec) {
            container.withDatabaseName(dbSpec.getDatabase())
                    .withUsername(dbSpec.getUsername())
                    .withPassword(dbSpec.getPassword())
                    .withEnv("MYSQL_ROOT_PASSWORD", dbSpec.getPassword());
        } else {
            container.withDatabaseName("primavera")
                    .withUsername("primavera")
                    .withPassword("primavera")
                    .withEnv("MYSQL_ROOT_PASSWORD", "primavera");
        }
        
        if (spec.getEnvironment() != null) {
            spec.getEnvironment().forEach(container::withEnv);
        }
        
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MYSQL;
    }
}