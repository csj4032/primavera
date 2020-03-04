package com.genius.primavera.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "primavera.config")
public class PrimaveraConfiguration {

	private String name = "primavera";
	private boolean enabled = false;

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