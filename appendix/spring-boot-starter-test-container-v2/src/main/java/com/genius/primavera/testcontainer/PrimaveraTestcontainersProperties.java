package com.genius.primavera.testcontainer;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "primavera.testcontainers")
public class PrimaveraTestcontainersProperties {

    private Map<String, ContainerConfig> containers = new HashMap<>();
    private ContainerLifecycleMode lifecycleMode = ContainerLifecycleMode.REUSE;

    public Map<String, ContainerConfig> getContainers() {
        return containers;
    }

    public void setContainers(Map<String, ContainerConfig> containers) {
        this.containers = containers;
    }

    public ContainerLifecycleMode getLifecycleMode() {
        return lifecycleMode;
    }

    public void setLifecycleMode(ContainerLifecycleMode lifecycleMode) {
        this.lifecycleMode = lifecycleMode;
    }

    public static class ContainerConfig {
        private boolean enabled = true;
        private String dockerImageName;
        private String driverClassName;
        private String databaseName;
        private String username;
        private String password;
        private String initScript;
        private int port;
        private Map<String, String> environment = new HashMap<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getDockerImageName() {
            return dockerImageName;
        }

        public void setDockerImageName(String dockerImageName) {
            this.dockerImageName = dockerImageName;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getInitScript() {
            return initScript;
        }

        public void setInitScript(String initScript) {
            this.initScript = initScript;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public Map<String, String> getEnvironment() {
            return environment;
        }

        public void setEnvironment(Map<String, String> environment) {
            this.environment = environment;
        }
    }
}