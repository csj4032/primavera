package com.genius.primavera;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;
import java.util.Map;

@Slf4j
@EnableConfigServer
@SpringBootApplication
public class ConfigurationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigurationApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void applicationReady() {
		log.info("Configuration application ready!!!");
	}
}

@Getter
@Setter
@ToString
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("primavera.config")
class PrimaveraConfiguration {
    private String name;
    private boolean enabled;
    private Map<String, String> logs;
    private String version;
    private String[] tags;
}