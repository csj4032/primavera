package com.genius.primavera.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

@Slf4j
public class TestContainerEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isTestProfile(environment)) {
            Properties props = new Properties();
            props.put("spring.jpa.hibernate.ddl-auto", "create-drop");
            props.put("spring.datasource.hikari.maximum-pool-size", "2");
            props.put("spring.datasource.hikari.minimum-idle", "1");
            props.put("logging.level.org.testcontainers", "INFO");
            props.put("logging.level.com.github.dockerjava", "WARN");
            environment.getPropertySources().addFirst(new PropertiesPropertySource("testcontainer-defaults", props));
        }
    }

    private boolean isTestProfile(ConfigurableEnvironment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("test".equals(profile)) return true;
        }
        return false;
    }
}