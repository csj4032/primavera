package com.genius.primavera.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "primavera.config")
public class PrimaveraConfiguration {

	private String name = "primavera";
	private boolean enabled;

	public PrimaveraConfiguration(String name, boolean enabled) {
		this.name = name;
		this.enabled = enabled;
	}

	public String getName() {
		return name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}