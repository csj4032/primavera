package com.genius.primavera.testContainer.config;

import org.springframework.core.env.Environment;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Optional;

public class PostgreSQLContainerConfig implements ContainerConfig<PostgreSQLContainer<?>> {

    // 프로퍼티 키 정의 (application-test.yml에서 사용할 키)
    private static final String IMAGE_KEY = "primavera.testcontainers.postgresql.image";
    private static final String DATABASE_NAME_KEY = "primavera.testcontainers.postgresql.database-name";
    private static final String USERNAME_KEY = "primavera.testcontainers.postgresql.username";
    private static final String PASSWORD_KEY = "primavera.testcontainers.postgresql.password";
    private static final String INIT_SCRIPT_KEY = "primavera.testcontainers.postgresql.init-script";

    // 기본값 정의
    private static final String DEFAULT_IMAGE = "postgres:14"; // 원하는 PostgreSQL 버전
    private static final String DEFAULT_DATABASE_NAME = "primavera_pg_test";
    private static final String DEFAULT_USERNAME = "pguser";
    private static final String DEFAULT_PASSWORD = "pgpass";

    public PostgreSQLContainerConfig() {
    }

    @Override
    public String getImageName() {
        return "postgres:14"; // 원하는 PostgreSQL 버전
    }

    @Override
    public PostgreSQLContainer<?> createContainer(Environment environment) {
        String image = environment.getProperty(IMAGE_KEY, DEFAULT_IMAGE);
        String databaseName = environment.getProperty(DATABASE_NAME_KEY, DEFAULT_DATABASE_NAME);
        String username = environment.getProperty(USERNAME_KEY, DEFAULT_USERNAME);
        String password = environment.getProperty(PASSWORD_KEY, DEFAULT_PASSWORD);
        Optional<String> initScript = Optional.ofNullable(environment.getProperty(INIT_SCRIPT_KEY));
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(image))
                .withDatabaseName(databaseName)
                .withUsername(username)
                .withPassword(password)
                .withReuse(true);

        initScript.ifPresent(container::withInitScript); // initScript가 있으면 적용

        return container;
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
