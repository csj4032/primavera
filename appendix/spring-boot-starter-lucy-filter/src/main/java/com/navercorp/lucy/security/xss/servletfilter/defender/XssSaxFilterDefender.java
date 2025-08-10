

package com.navercorp.lucy.security.xss.servletfilter.defender;

import com.nhncorp.lucy.security.xss.XssSaxFilter;
import org.apache.commons.lang3.StringUtils;

public class XssSaxFilterDefender implements Defender {
	private XssSaxFilter filter;

	@Override
	public void init(String[] values) {
		if (values == null || values.length == 0) {
			filter = XssSaxFilter.getInstance();
		} else {
			switch (values.length) {
				case 1:
					if (isBoolean(values[0])) {
						filter = XssSaxFilter.getInstance(convertBoolean(values[0]));	
					} else {
						filter = XssSaxFilter.getInstance(values[0]);
					}
					break;
				case 2:
					filter = XssSaxFilter.getInstance(values[0], convertBoolean(values[1]));	
					break;
				default:
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
