package com.genius.primavera.test;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.containers.MariaDBContainer;

@Slf4j
@Getter
public class TestContainerService {

    private final MariaDBContainer<?> mariaDBContainer;

    // 프로퍼티 기반 생성자
    public TestContainerService(
            @Value("${primavera.testcontainers.database-name:primavera}") String databaseName,
            @Value("${primavera.testcontainers.username:primavera}") String username,
            @Value("${primavera.testcontainers.password:primavera}") String password,
            @Value("${primavera.testcontainers.mariadb-version:mariadb:11.4.7}") String mariadbVersion,
            @Value("${primavera.testcontainers.init-script:sql/schema.sql}") String initScript) {
        
        log.info("Creating TestContainerService with properties - databaseName: {}", databaseName);
        
        this.mariaDBContainer = new MariaDBContainer<>(mariadbVersion)
                .withDatabaseName(databaseName)
                .withUsername(username)
                .withPassword(password)
                .withCommand("--default-authentication-plugin=mysql_native_password");
        
        if (initScript != null && !initScript.isEmpty() && !"none".equals(initScript)) {
            this.mariaDBContainer.withInitScript(initScript);
        }
        
        if (!this.mariaDBContainer.isRunning()) {
            this.mariaDBContainer.start();
        }
    }

    // 기본 생성자 (기존 호환성 유지)
    public TestContainerService() {
        log.info("Creating TestContainerService with default settings");
        this.mariaDBContainer = MariaDBContainerFactory.createFromTestClass();
        if (!this.mariaDBContainer.isRunning()) {
            this.mariaDBContainer.start();
        }
    }

    public String getJdbcUrl() {
        return mariaDBContainer.getJdbcUrl() + "?allowPublicKeyRetrieval=true&useSSL=false";
    }

    public String getUsername() {
        return mariaDBContainer.getUsername();
    }

    public String getPassword() {
        return mariaDBContainer.getPassword();
    }

    public String getHost() {
        return mariaDBContainer.getHost();
    }

    public Integer getMappedPort() {
        return mariaDBContainer.getMappedPort(3306);
    }

    public boolean isRunning() {
        return mariaDBContainer.isRunning();
    }
}