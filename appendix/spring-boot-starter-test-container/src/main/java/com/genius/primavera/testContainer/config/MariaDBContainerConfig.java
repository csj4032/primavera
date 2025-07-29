package com.genius.primavera.testContainer.config;

import com.genius.primavera.testContainer.PrimaveraTestcontainersProperties;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Optional;

public class MariaDBContainerConfig implements ContainerConfig<MariaDBContainer<?>> {

    private final PrimaveraTestcontainersProperties.Mariadb mariadbProperties;

    public MariaDBContainerConfig(PrimaveraTestcontainersProperties properties) {
        this.mariadbProperties = properties.getMariadb();
    }

    @Override
    public String getImageName() {
        return "mariadb:10.6";
    }

    @Override
    public MariaDBContainer<?> createContainer() {
        String image = mariadbProperties.getImage();
        String databaseName = mariadbProperties.getDatabaseName();
        String username = mariadbProperties.getUsername();
        String password = mariadbProperties.getPassword();
        Optional<String> initScript = Optional.ofNullable(mariadbProperties.getInitScript());

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