package com.genius.primavera.testContainer.config;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Optional;

public class MariaDBContainerConfig implements ContainerConfig<MariaDBContainer<?>> {

    private static final String IMAGE_KEY = "primavera.testcontainers.mariadb.image";
    private static final String DATABASE_NAME_KEY = "primavera.testcontainers.mariadb.database-name";
    private static final String USERNAME_KEY = "primavera.testcontainers.mariadb.username";
    private static final String PASSWORD_KEY = "primavera.testcontainers.mariadb.password";
    private static final String INIT_SCRIPT_KEY = "primavera.testcontainers.mariadb.init-script"; // <--- 추가

    private static final String DEFAULT_IMAGE = "mariadb:10.6";
    private static final String DEFAULT_DATABASE_NAME = "primavera_basic_test";
    private static final String DEFAULT_USERNAME = "primavera";
    private static final String DEFAULT_PASSWORD = "testpass";

    public MariaDBContainerConfig() {
    }

    @Override
    public String getImageName() {
        return "mariadb:10.6";
    }

    @Override
    public MariaDBContainer<?> createContainer(Environment environment) {
        String image = environment.getProperty(IMAGE_KEY, DEFAULT_IMAGE);
        String databaseName = environment.getProperty(DATABASE_NAME_KEY, DEFAULT_DATABASE_NAME);
        String username = environment.getProperty(USERNAME_KEY, DEFAULT_USERNAME);
        String password = environment.getProperty(PASSWORD_KEY, DEFAULT_PASSWORD);
        Optional<String> initScript = Optional.ofNullable(environment.getProperty(INIT_SCRIPT_KEY));

        MariaDBContainer<?> container = new MariaDBContainer<>(DockerImageName.parse(image))
                .withDatabaseName(databaseName)
                .withUsername(username)
                .withPassword(password)
                .withReuse(true);

        initScript.ifPresent(container::withInitScript);

        return container;
    }

    @Override
    public Map<String, Object> getSpringProperties(MariaDBContainer<?> container, Environment environment) {
        return Map.of(
                "spring.datasource.url", container.getJdbcUrl(),
                "spring.datasource.username", container.getUsername(),
                "spring.datasource.password", container.getPassword(),
                "spring.datasource.driver-class-name", container.getDriverClassName()
        );
    }
}