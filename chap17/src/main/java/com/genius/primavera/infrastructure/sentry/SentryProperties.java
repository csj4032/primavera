package com.genius.primavera.infrastructure.sentry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "sentry")
public class SentryProperties {
	private String dns;
	private String environment;
	private String servername;
	private String release;
}