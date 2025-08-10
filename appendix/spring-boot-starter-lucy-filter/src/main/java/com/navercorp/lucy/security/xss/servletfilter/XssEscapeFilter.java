

package com.navercorp.lucy.security.xss.servletfilter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class XssEscapeFilter {
	private static final Log LOG = LogFactory.getLog(XssEscapeFilter.class);

	private static XssEscapeFilter xssEscapeFilter;
	private static XssEscapeFilterConfig config;

	static {
		try {
			xssEscapeFilter = new XssEscapeFilter();
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private XssEscapeFilter() {
		config = new XssEscapeFilterConfig();
	}

	public static XssEscapeFilter getInstance() {
		return xssEscapeFilter;
	}

	public String doFilter(String url, String paramName, String value) {
		if (StringUtils.isBlank(value)) {
			return value;
		}

		XssEscapeFilterRule urlRule = config.getUrlParamRule(url, paramName);
		if (urlRule == null) {

			return config.getDefaultDefender().doFilter(value);
		} 

		if (!urlRule.isUseDefender()) {
			log(url, paramName, value);
			return value;
		}

		return urlRule.getDefender().doFilter(value);
	}

	private void log(String url, String paramName, String value) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Do not filtered Parameter. Request url: " + url + ", Parameter name: " + paramName + ", Parameter value: " + value);
		}
	}
}
