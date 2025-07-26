package com.genius.primavera.test;

import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.MariaDBContainer;

import java.util.Arrays;

@Component
@Getter
public class TestContainerService {

    private final MariaDBContainer<?> mariaDBContainer;
    private final PrimaveraTestContainer config;

    public TestContainerService() {
        Class<?> testClass = MariaDBContainerFactory.findTestClass();
        this.config = testClass != null ? testClass.getAnnotation(PrimaveraTestContainer.class) : null;
        this.mariaDBContainer = createMariaDBContainer();
        if (!this.mariaDBContainer.isRunning()) this.mariaDBContainer.start();
    }

    private MariaDBContainer<?> createMariaDBContainer() {
        if (config != null) {
            MariaDBContainer<?> container = new MariaDBContainer<>(config.mariadbVersion())
                    .withDatabaseName(config.databaseName())
                    .withUsername(config.username())
                    .withPassword(config.password())
                    .withCommand("--default-authentication-plugin=mysql_native_password");
            if (config.enableInitScript() && !config.initScript().isEmpty() && !"none".equals(config.initScript())) container.withInitScript(config.initScript());
            return container;
        }

        return new MariaDBContainer<>("mariadb:11.4.7")
                .withDatabaseName("primavera")
                .withUsername("primavera")
                .withPassword("primavera")
                .withInitScript("sql/schema.sql")
                .withCommand("--default-authentication-plugin=mysql_native_password");
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