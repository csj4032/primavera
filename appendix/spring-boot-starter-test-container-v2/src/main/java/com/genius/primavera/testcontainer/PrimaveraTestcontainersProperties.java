package com.genius.primavera.testcontainer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "primavera.testcontainers.mariadb")
public class PrimaveraTestcontainersProperties {
    
    private boolean enabled = true;
    private String dockerImageName = "mariadb:11.4.7";
    private String driverClassName = "org.mariadb.jdbc.Driver";
    private String databaseName = "primavera";
    private String username = "primavera";
    private String password = "primavera";
    private String initScript;
    
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
}