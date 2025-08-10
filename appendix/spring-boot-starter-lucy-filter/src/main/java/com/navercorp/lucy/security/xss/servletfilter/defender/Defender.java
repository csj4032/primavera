

package com.navercorp.lucy.security.xss.servletfilter.defender;

public interface Defender {
	public abstract void init(String[] values);
	public abstract String doFilter(String value);
}
