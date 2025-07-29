package com.genius.primavera.testContainer.config;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Optional;

public class PostgreSQLContainerConfig implements ContainerConfig<PostgreSQLContainer<?>> {

    private final PrimaveraTestcontainersProperties.PostgreSQL postgreSQLProperties;

    public PostgreSQLContainerConfig(PrimaveraTestcontainersProperties properties) {
        this.postgreSQLProperties = properties.getPostgreSQL();
    }

    @Override
    public String getImageName() {
        return "postgres:14"; // 원하는 PostgreSQL 버전
    }

    @Override
    public PostgreSQLContainer<?> createContainer() {
        String image = postgreSQLProperties.getImage();
        String databaseName = postgreSQLProperties.getDatabaseName();
        String username = postgreSQLProperties.getUsername();
        String password = postgreSQLProperties.getPassword();
        Optional<String> initScript = Optional.ofNullable(postgreSQLProperties.getInitScript());
        return new PostgreSQLContainer<>(image)
                .withDatabaseName(databaseName)
                .withUsername(username)
                .withPassword(password)
                .withReuse(true);
    }

    @Override
    public Map<String, Object> getSpringProperties(PostgreSQLContainer<?> container, Environment environment) {
        return Map.of(
                "spring.datasource.url", container.getJdbcUrl(),
                "spring.datasource.username", container.getUsername(),
                "spring.datasource.password", container.getPassword(),
                "spring.datasource.driver-class-name", container.getDriverClassName()
        );
    }
}
