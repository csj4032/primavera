

package com.navercorp.lucy.security.xss.servletfilter.defender;

import com.nhncorp.lucy.security.xss.XssPreventer;

public class XssPreventerDefender implements Defender {

	@Override
	public void init(String[] values) {
	}

	@Override
	public String doFilter(String value) {
		return XssPreventer.escape(value);
	}
}
