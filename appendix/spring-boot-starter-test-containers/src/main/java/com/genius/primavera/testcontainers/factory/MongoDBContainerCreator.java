package com.genius.primavera.testcontainers.factory;

import com.genius.primavera.testcontainers.ContainerCreator;
import com.genius.primavera.testcontainers.ContainerType;
import com.genius.primavera.testcontainers.config.BaseContainerSpec;
import com.genius.primavera.testcontainers.config.MongoContainerSpec;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class MongoDBContainerCreator implements ContainerCreator {
    
    @Override
    public GenericContainer<?> create(BaseContainerSpec spec) {
        String image = spec.getImage() != null ? spec.getImage() : ContainerType.MONGODB.getDefaultImage();
        Integer timeout = spec.getStartupTimeout() != null ? spec.getStartupTimeout() : 60;
        
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(image))
                .withExposedPorts(27017)
                .withStartupTimeout(Duration.ofSeconds(timeout));
        
        if (spec instanceof MongoContainerSpec mongoSpec) {
            if (mongoSpec.getUsername() != null && mongoSpec.getPassword() != null) {
                container.withEnv("MONGO_INITDB_ROOT_USERNAME", mongoSpec.getUsername())
                        .withEnv("MONGO_INITDB_ROOT_PASSWORD", mongoSpec.getPassword());
            }
            
            if (mongoSpec.getDatabase() != null) {
                container.withEnv("MONGO_INITDB_DATABASE", mongoSpec.getDatabase());
            }
            
            if (mongoSpec.getAuthDatabase() != null) {
                container.withEnv("MONGO_INITDB_AUTH_SOURCE", mongoSpec.getAuthDatabase());
            }
            
            if (mongoSpec.getWiredTigerCacheSizeMB() != null) {
                container.withEnv("MONGO_WIRED_TIGER_CACHE_SIZE_GB", String.valueOf(mongoSpec.getWiredTigerCacheSizeMB() / 1024.0));
            }
        } else {
            // 기본 설정
            container.withEnv("MONGO_INITDB_ROOT_USERNAME", "primavera")
                    .withEnv("MONGO_INITDB_ROOT_PASSWORD", "primavera")
                    .withEnv("MONGO_INITDB_DATABASE", "primavera");
        }
        
        // 공통 환경 변수 적용
        if (spec.getEnvironment() != null) {
            spec.getEnvironment().forEach(container::withEnv);
        }
        
        return container;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MONGODB;
    }
}