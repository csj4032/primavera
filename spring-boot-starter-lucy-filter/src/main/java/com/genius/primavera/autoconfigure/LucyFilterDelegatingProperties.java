package com.genius.primavera.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.lucy-filter")
public class LucyFilterDelegatingProperties {
	public String name;
	private int order;
	private String[] addUrlPatterns;
}
