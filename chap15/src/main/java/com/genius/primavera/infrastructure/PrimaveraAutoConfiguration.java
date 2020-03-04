package com.genius.primavera.infrastructure;

import com.genius.primavera.domain.Primavera;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(PrimaveraConfiguration.class)
public class PrimaveraAutoConfiguration {

	@Bean
	public Primavera primaveraConfig(PrimaveraConfiguration properties) {
		return new Primavera(properties.getName());
	}
}