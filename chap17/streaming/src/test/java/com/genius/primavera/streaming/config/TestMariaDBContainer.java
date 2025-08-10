package com.genius.primavera.streaming.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestMariaDBContainer {

    @Bean
    @Primary
    public MariaDBContainer<?> mariaDBContainer() {
        MariaDBContainer<?> container = new MariaDBContainer<>(DockerImageName.parse("mariadb:11.4.7"))
                .withDatabaseName("primavera")
                .withUsername("primavera")
                .withPassword("primavera")
                .withCommand(
                    "--server-id=1",
                    "--log-bin=mysql-bin",
                    "--binlog-format=ROW",
                    "--binlog-row-image=FULL",
                    "--gtid-mode=ON",
                    "--enforce-gtid-consistency=ON"
                )
                .withInitScript("sql/init.sql");
        
        container.start();
        return container;
    }
}