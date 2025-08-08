package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.Optional;

@Slf4j
class ContainerConfigurationHelper {

    static void configureContainer(GenericContainer<?> container, ContainerConfiguration.ContainerSpec spec) {
        Optional.ofNullable(spec.getEnvironment())
                .ifPresent(container::withEnv);

        Optional.ofNullable(spec.getNetworkAliases())
                .ifPresent(aliases -> {
                    for (String alias : aliases) {
                        container.withNetworkAliases(alias);
                    }
                });

        Optional.ofNullable(spec.getInitScript())
                .ifPresent(initScript -> {
                    if (container instanceof JdbcDatabaseContainer<?> jdbcContainer) {
                        String scriptPath = initScript.startsWith("classpath:") ? initScript.substring("classpath:".length()) : initScript;
                        if (scriptPath.startsWith("./")) scriptPath = scriptPath.substring(2);
                        jdbcContainer.withInitScript(scriptPath);
                        log.info("Configured init script: {} for database container", scriptPath);
                    } else {
                        log.warn("Init script '{}' specified but container is not a database container", initScript);
                    }
                });
    }
}