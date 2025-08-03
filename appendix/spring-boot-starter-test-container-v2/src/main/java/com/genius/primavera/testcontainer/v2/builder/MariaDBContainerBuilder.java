package com.genius.primavera.testcontainer.v2.builder;

import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

public class MariaDBContainerBuilder implements ContainerBuilder<MariaDBContainer<?>> {
    
    private String dockerImageName = "mariadb:11.4.7";
    private String databaseName = "primavera";
    private String username = "primavera";
    private String password = "primavera";
    private String initScript;
    private final Map<String, String> environment = new HashMap<>();
    
    @Override
    public ContainerBuilder<MariaDBContainer<?>> withImage(String dockerImageName) {
        this.dockerImageName = dockerImageName;
        return this;
    }
    
    @Override
    public ContainerBuilder<MariaDBContainer<?>> withDatabase(String databaseName) {
        this.databaseName = databaseName;
        return this;
    }
    
    @Override
    public ContainerBuilder<MariaDBContainer<?>> withCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }
    
    @Override
    public ContainerBuilder<MariaDBContainer<?>> withInitScript(String initScript) {
        this.initScript = initScript;
        return this;
    }
    
    @Override
    public ContainerBuilder<MariaDBContainer<?>> withPorts(int... ports) {
        // MariaDB는 기본 포트를 사용하므로 무시
        return this;
    }
    
    @Override
    public ContainerBuilder<MariaDBContainer<?>> withEnvironment(String key, String value) {
        this.environment.put(key, value);
        return this;
    }
    
    @Override
    public MariaDBContainer<?> build() {
        MariaDBContainer<?> container = new MariaDBContainer<>(DockerImageName.parse(dockerImageName))
                .withDatabaseName(databaseName)
                .withUsername(username)
                .withPassword(password);
        
        if (initScript != null && !initScript.isEmpty()) {
            container.withInitScript(initScript);
        }
        
        environment.forEach(container::withEnv);
        
        return container;
    }
}