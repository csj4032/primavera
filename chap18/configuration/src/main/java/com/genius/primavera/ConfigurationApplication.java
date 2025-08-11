package com.genius.primavera;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.event.EventListener;

import java.util.Map;

@Slf4j
@EnableConfigServer
@SpringBootApplication
@EnableConfigurationProperties(PrimaveraConfiguration.class)
public class ConfigurationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigurationApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady() {
		log.info("configuration application ready!!!");
	}
}

@Getter
@Setter
@ConfigurationProperties("primavera.config")
class PrimaveraConfiguration {

    private String name;
    private boolean enabled;
    private Map<String, String> logs;

    public PrimaveraConfiguration(String name, boolean enabled, Map<String, String> logs) {
        this.name = name;
        this.enabled = enabled;
        this.logs = logs;
    }
}