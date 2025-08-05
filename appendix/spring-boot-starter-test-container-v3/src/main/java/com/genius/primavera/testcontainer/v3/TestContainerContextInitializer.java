package com.genius.primavera.testcontainer.v3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring ApplicationContext 초기화 시 TestContainer 정보를 프로퍼티로 등록 (v3)
 */
@Slf4j
public class TestContainerContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private static final String PROPERTY_SOURCE_NAME = "testContainerProperties";
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        // ContainerManager에서 컨테이너 정보 가져오기
        ContainerManager containerManager = getContainerManager(applicationContext);
        if (containerManager == null) {
            log.debug("No ContainerManager found, skipping container property registration");
            return;
        }
        
        // 컨테이너가 아직 시작되지 않았다면 시작
        if (!containerManager.isStarted()) {
            log.info("Starting containers during Spring context initialization");
            containerManager.startContainers();
        }
        
        // 컨테이너 정보를 Spring 프로퍼티로 변환
        Map<String, Object> properties = new HashMap<>();
        
        containerManager.getAllContainers().forEach(containerInfo -> {
            String prefix = "testcontainer.runtime." + containerInfo.getName();
            
            // 공통 런타임 프로퍼티
            properties.put(prefix + ".host", containerInfo.getHost());
            properties.put(prefix + ".port", containerInfo.getMappedPort());
            properties.put(prefix + ".type", containerInfo.getType().name());
            
            // SQL 데이터베이스 전용 프로퍼티
            if (containerInfo.getType().isSqlDatabase()) {
                properties.put(prefix + ".jdbc-url", containerInfo.getJdbcUrl());
                properties.put(prefix + ".driver-class-name", containerInfo.getType().getDriverClassName());
                properties.put(prefix + ".username", containerInfo.getConfig().getUsername());
                properties.put(prefix + ".password", containerInfo.getConfig().getPassword());
                properties.put(prefix + ".database", containerInfo.getConfig().getDatabase());
            }
            
            // Redis 전용 프로퍼티
            if (containerInfo.getType() == ContainerType.REDIS) {
                String redisPrefix = "spring.data.redis." + containerInfo.getName();
                properties.put(redisPrefix + ".host", containerInfo.getHost());
                properties.put(redisPrefix + ".port", containerInfo.getMappedPort());
                
                if (containerInfo.getConfig().getPassword() != null && 
                    !containerInfo.getConfig().getPassword().isEmpty()) {
                    properties.put(redisPrefix + ".password", containerInfo.getConfig().getPassword());
                }
            }
            
            // MongoDB 전용 프로퍼티
            if (containerInfo.getType() == ContainerType.MONGODB) {
                String mongoPrefix = "spring.data.mongodb." + containerInfo.getName();
                String connectionString = String.format("mongodb://%s:%d/%s", 
                    containerInfo.getHost(), containerInfo.getMappedPort(), 
                    containerInfo.getConfig().getDatabase());
                properties.put(mongoPrefix + ".uri", connectionString);
            }
            
            // Kafka 전용 프로퍼티
            if (containerInfo.getType() == ContainerType.KAFKA) {
                String kafkaPrefix = "spring.kafka." + containerInfo.getName();
                properties.put(kafkaPrefix + ".bootstrap-servers", 
                    containerInfo.getHost() + ":" + containerInfo.getMappedPort());
            }
            
            // Elasticsearch 전용 프로퍼티
            if (containerInfo.getType() == ContainerType.ELASTICSEARCH) {
                String esPrefix = "spring.elasticsearch." + containerInfo.getName();
                properties.put(esPrefix + ".uris", 
                    "http://" + containerInfo.getHost() + ":" + containerInfo.getMappedPort());
            }
        });
        
        // 프로퍼티 소스 추가
        if (!properties.isEmpty()) {
            MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, properties);
            environment.getPropertySources().addFirst(propertySource);
            
            log.info("Registered {} TestContainer runtime properties", properties.size());
        }
        
        // Bean 팩토리 후처리기 추가 (동적 Bean 등록)
        applicationContext.addBeanFactoryPostProcessor(beanFactory -> {
            BeanRegistrar beanRegistrar = new BeanRegistrar(containerManager);
            beanRegistrar.registerBeans(beanFactory);
        });
    }
    
    /**
     * ContainerManager 조회
     */
    private ContainerManager getContainerManager(ConfigurableApplicationContext context) {
        return ContainerManagerHolder.get();
    }
}