package com.genius.primavera;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@ToString
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("config")
public class Config {
	private String version;
	private String[] tags;
	private List<Category> categories;
}