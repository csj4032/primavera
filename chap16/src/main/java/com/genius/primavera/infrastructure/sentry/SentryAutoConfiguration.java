package com.genius.primavera.infrastructure.sentry;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Sentry.class)
@EnableConfigurationProperties(SentryProperties.class)
public class SentryAutoConfiguration {

	@Bean
	public SentryClient sentry(SentryProperties properties) {
		SentryClient sentryClient = Sentry.init(properties.getDns());
		sentryClient.setEnvironment(properties.getEnvironment());
		sentryClient.setServerName(properties.getServername());
		sentryClient.setRelease(properties.getRelease());
		return sentryClient;
	}
}