

package com.navercorp.lucy.security.xss.servletfilter.defender;

import com.nhncorp.lucy.security.xss.XssFilter;
import org.apache.commons.lang3.StringUtils;

public class XssFilterDefender implements Defender {
	private XssFilter filter;

	@Override
	public void init(String[] values) {
		if (values == null || values.length == 0) {
			filter = XssFilter.getInstance();
		} else {
			switch (values.length) {
				case 1:
					if (isBoolean(values[0])) {
						filter = XssFilter.getInstance(convertBoolean(values[0]));	
					} else {
						filter = XssFilter.getInstance(values[0]);
					}
					break;
				case 2:
					filter = XssFilter.getInstance(values[0], convertBoolean(values[1]));	
					break;
				default:
					filter = null;
					break;
			}
		}
	}

	@Override
	public String doFilter(String value) {
		return filter.doFilter(value);
	}

	private boolean isBoolean(String value) {
		return StringUtils.equalsIgnoreCase(value, "true") || StringUtils.equalsIgnoreCase(value, "false");
	}

	private boolean convertBoolean(String value) {
		return StringUtils.equalsIgnoreCase(value, "true") ? true : false;
	}
}
