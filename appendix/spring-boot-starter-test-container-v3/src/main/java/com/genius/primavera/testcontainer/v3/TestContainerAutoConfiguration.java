package com.genius.primavera.testcontainer.v3;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * TestContainer 자동 구성 (v3)
 */
@AutoConfiguration
@EnableConfigurationProperties(TestContainerProperties.class)
@Conditional(TestContainerCondition.class)
public class TestContainerAutoConfiguration {
    
    @Bean
    public TestContainerProperties testContainerProperties() {
        return new TestContainerProperties();
    }
}