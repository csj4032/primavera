package com.genius.primavera.testcontainer;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(ContainerConfiguration.class)
public class TestContainerAutoConfiguration {
}