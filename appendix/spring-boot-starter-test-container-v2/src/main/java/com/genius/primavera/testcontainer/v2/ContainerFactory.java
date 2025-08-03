package com.genius.primavera.testcontainer.v2;

import com.genius.primavera.testcontainer.v2.factory.ContainerBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;

@Slf4j
public class ContainerFactory {
    
    public static GenericContainer<?> createContainer(ContainerType containerType, TestContainerProperties.ContainerConfig config) {
        ContainerBuilderFactory factory = ContainerBuilderFactory.getFactory(containerType);
        return factory.createContainer(config);
    }
}