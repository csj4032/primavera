package com.genius.primavera.application;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties("mariadb")
public class MariaDB {
	private String url;
	private String username;
	private String password;
	private List<String> allows;
}
