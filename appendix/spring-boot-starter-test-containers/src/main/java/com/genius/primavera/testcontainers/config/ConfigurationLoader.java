package com.genius.primavera.testcontainers.config;

import com.genius.primavera.testcontainers.ContainerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePropertySource;

import java.util.List;

/**
 * TestContainers 설정 파일 로딩을 담당하는 클래스
 * YAML 및 Properties 파일을 읽어서 ContainerConfiguration 객체로 바인딩
 */
@Slf4j
public class ConfigurationLoader {
    
    private static final String[] YAML_FILES = {"application-test.yml", "application-test.yaml"};
    private static final String[] PROPERTIES_FILES = {"application-test.properties"};
    private static final String CONFIGURATION_PREFIX = "testcontainers";
    
    /**
     * 클래스패스에서 설정 파일을 로드하여 ContainerConfiguration 반환
     * 
     * @return ContainerConfiguration 설정 객체
     */
    public ContainerConfiguration loadConfiguration() {
        try {
            ConfigurableEnvironment environment = createEnvironment();
            loadYamlFiles(environment);
            loadPropertiesFiles(environment);
            
            ContainerConfiguration config = bindConfiguration(environment);
            logLoadedConfiguration(config);
            
            return config;
        } catch (Exception e) {
            log.warn("Failed to load configuration, using defaults: {}", e.getMessage());
            return new ContainerConfiguration();
        }
    }
    
    private ConfigurableEnvironment createEnvironment() {
        return new StandardEnvironment();
    }
    
    private void loadYamlFiles(ConfigurableEnvironment environment) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        YamlPropertySourceLoader yamlLoader = new YamlPropertySourceLoader();
        
        for (String yamlFile : YAML_FILES) {
            try {
                Resource resource = resolver.getResource("classpath:" + yamlFile);
                if (resource.exists()) {
                    log.info("Loading YAML configuration from '{}'", yamlFile);
                    List<PropertySource<?>> propertySources = yamlLoader.load(yamlFile, resource);
                    for (PropertySource<?> propertySource : propertySources) {
                        environment.getPropertySources().addFirst(propertySource);
                    }
                    return; // 첫 번째로 발견된 파일만 로드
                }
            } catch (Exception e) {
                log.warn("Failed to load YAML configuration from '{}': {}", yamlFile, e.getMessage());
            }
        }
        
        log.debug("No YAML configuration files found");
    }
    
    private void loadPropertiesFiles(ConfigurableEnvironment environment) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        
        for (String propFile : PROPERTIES_FILES) {
            try {
                Resource resource = resolver.getResource("classpath:" + propFile);
                if (resource.exists()) {
                    log.info("Loading properties configuration from '{}'", propFile);
                    ResourcePropertySource source = new ResourcePropertySource(propFile, resource);
                    environment.getPropertySources().addFirst(source);
                    return; // 첫 번째로 발견된 파일만 로드
                }
            } catch (Exception e) {
                log.warn("Failed to load properties configuration from '{}': {}", propFile, e.getMessage());
            }
        }
        
        log.debug("No properties configuration files found");
    }
    
    private ContainerConfiguration bindConfiguration(ConfigurableEnvironment environment) {
        return Binder.get(environment)
                .bind(CONFIGURATION_PREFIX, ContainerConfiguration.class)
                .orElse(new ContainerConfiguration());
    }
    
    private void logLoadedConfiguration(ContainerConfiguration config) {
        if (log.isDebugEnabled()) {
            log.debug("Loaded configuration with {} containers", config.getContainers().size());
            config.getContainers().forEach((name, instanceConfig) -> {
                log.debug("Container '{}': type={}, spec={}", 
                    name, 
                    instanceConfig.getType(), 
                    instanceConfig.getSpecForType() != null ? 
                        instanceConfig.getSpecForType().getClass().getSimpleName() : "null"
                );
            });
        }
    }
}