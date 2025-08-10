

package com.navercorp.lucy.security.xss.servletfilter;

import com.navercorp.lucy.security.xss.servletfilter.defender.Defender;

public class XssEscapeFilterRule {
	private String name;
	private boolean useDefender = true;
	private Defender defender;
	private boolean usePrefix = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isUseDefender() {
		return useDefender;
	}

	public void setUseDefender(boolean useDefender) {
		this.useDefender = useDefender;
	}

	public Defender getDefender() {
		return defender;
	}

	public void setDefender(Defender defender) {
		this.defender = defender;
	}

	public boolean isUsePrefix() {
		return usePrefix;
	}

	public void setUsePrefix(boolean usePrefix) {
		this.usePrefix = usePrefix;
	}
}
