package com.genius.primavera.testcontainer.v2.builder;

import org.testcontainers.containers.GenericContainer;

public interface ContainerBuilder<T extends GenericContainer<?>> {
    
    ContainerBuilder<T> withImage(String dockerImageName);
    
    ContainerBuilder<T> withDatabase(String databaseName);
    
    ContainerBuilder<T> withCredentials(String username, String password);
    
    ContainerBuilder<T> withInitScript(String initScript);
    
    ContainerBuilder<T> withPorts(int... ports);
    
    ContainerBuilder<T> withEnvironment(String key, String value);
    
    T build();
}