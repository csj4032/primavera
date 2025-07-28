package com.genius.primavera.testContainer;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@ConfigurationProperties(prefix = "primavera.testcontainers")
public class PrimaveraTestcontainersProperties {

    private final Mariadb mariadb = new Mariadb();

    @Getter
    @Setter
    @ToString
    public static class Mariadb {
        private String dockerImageName = "mariadb:11.4.7";
        private String driverClassName = "org.mariadb.jdbc.Driver";
        private String databaseName = "primavera";
        private String host = "localhost";
        private String username = "primavera";
        private String password = "primavera";
        private int port = 3306;
        private String initScript;
    }
}