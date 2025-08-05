package com.genius.primavera.testcontainer.v4.factory;

import com.genius.primavera.testcontainer.v4.ContainerConfiguration;
import org.testcontainers.containers.GenericContainer;

import java.util.Optional;

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
    }
}