package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.DatabaseContainerSpec;
import com.genius.primavera.testcontainers.config.MariaDbContainerSpec;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@Slf4j
public class MariaDBContainerCreator implements ContainerCreator {

    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        log.info("Received spec type: {}", spec.getClass().getSimpleName());

        String image = spec.getImage() != null ? spec.getImage() : ContainerType.MARIADB.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;

        MariaDBContainer<?> container = new MariaDBContainer<>(DockerImageName.parse(image))
                .withStartupTimeout(Duration.ofSeconds(timeout));

        if (spec instanceof MariaDbContainerSpec mariaDbSpec) {
            log.info("Using MariaDbContainerSpec {} ", mariaDbSpec.getRootPassword());
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

            log.info("MariaDbContainerSpec details:");
            log.info("  - database: {}", mariaDbSpec.getDatabase());
            log.info("  - username: {}", mariaDbSpec.getUsername());
            log.info("  - initScript: {}", mariaDbSpec.getInitScript());
            log.info("  - image: {}", mariaDbSpec.getImage());
            log.info("  - command: {}", mariaDbSpec.getCommand());

            // Apply custom command if specified
            if (mariaDbSpec.getCommand() != null && !mariaDbSpec.getCommand().isEmpty()) {
                log.info("Applying custom command: {}", mariaDbSpec.getCommand());
                container.withCommand(mariaDbSpec.getCommand().toArray(new String[0]));
            }

            if (mariaDbSpec.getInitScript() != null) {
                log.info("Processing initScript: {}", mariaDbSpec.getInitScript());
                String scriptPath = mariaDbSpec.getInitScript();
                if (scriptPath.startsWith("classpath:")) {
                    scriptPath = scriptPath.substring("classpath:".length());
                }
                log.info("Final script path: {}", scriptPath);
                container.withInitScript(scriptPath);
            } else {
                log.warn("InitScript is null in MariaDbContainerSpec!");
            }
        } else if (spec instanceof DatabaseContainerSpec dbSpec) {
            log.info("Using DatabaseContainerSpec");
            container.withDatabaseName(dbSpec.getDatabase())
                    .withUsername(dbSpec.getUsername())
                    .withPassword(dbSpec.getPassword())
                    .withEnv("MARIADB_ROOT_PASSWORD", dbSpec.getPassword());

            log.info("DatabaseContainerSpec initScript: {}", dbSpec.getInitScript());
            if (dbSpec.getInitScript() != null) {
                String scriptPath = dbSpec.getInitScript();
                if (scriptPath.startsWith("classpath:")) {
                    scriptPath = scriptPath.substring("classpath:".length());
                }
                container.withInitScript(scriptPath);
            }
        } else {
            log.info("Using default configuration - spec type: {}", spec.getClass().getSimpleName());
            container.withDatabaseName("primavera")
                    .withUsername("primavera")
                    .withPassword("primavera")
                    .withEnv("MARIADB_ROOT_PASSWORD", "primavera");
        }

        if (spec.getEnvironment() != null) spec.getEnvironment().forEach(container::withEnv);

        return container;
    }

    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MARIADB;
    }
}