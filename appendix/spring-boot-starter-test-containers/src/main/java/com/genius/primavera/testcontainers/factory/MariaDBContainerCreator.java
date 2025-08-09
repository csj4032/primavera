package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.DatabaseContainerSpec;
import com.genius.primavera.testcontainers.config.MariaDbContainerSpec;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class MariaDBContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        String image = spec.getImage() != null ? spec.getImage() : ContainerType.MARIADB.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;
        
        MariaDBContainer<?> container = new MariaDBContainer<>(DockerImageName.parse(image))
                .withStartupTimeout(Duration.ofSeconds(timeout));
        
        if (spec instanceof MariaDbContainerSpec mariaDbSpec) {
            container.withDatabaseName(mariaDbSpec.getDatabase())
                    .withUsername(mariaDbSpec.getUsername())
                    .withPassword(mariaDbSpec.getPassword())
                    .withEnv("MARIADB_ROOT_PASSWORD", mariaDbSpec.getRootPassword());
                    
            if (mariaDbSpec.getCharacterSet() != null) {
                container.withEnv("MARIADB_CHARACTER_SET_SERVER", mariaDbSpec.getCharacterSet());
            }
            if (mariaDbSpec.getCollation() != null) {
                container.withEnv("MARIADB_COLLATION_SERVER", mariaDbSpec.getCollation());
            }
        } else if (spec instanceof DatabaseContainerSpec dbSpec) {
            container.withDatabaseName(dbSpec.getDatabase())
                    .withUsername(dbSpec.getUsername())
                    .withPassword(dbSpec.getPassword())
                    .withEnv("MARIADB_ROOT_PASSWORD", dbSpec.getPassword());
        } else {
            container.withDatabaseName("primavera")
                    .withUsername("primavera")
                    .withPassword("primavera")
                    .withEnv("MARIADB_ROOT_PASSWORD", "primavera");
        }
        
        // 공통 환경 변수 적용
        if (spec.getEnvironment() != null) {
            spec.getEnvironment().forEach(container::withEnv);
        }
        
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MARIADB;
    }
}