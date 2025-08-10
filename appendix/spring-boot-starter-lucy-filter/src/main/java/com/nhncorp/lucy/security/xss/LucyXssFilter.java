
package com.nhncorp.lucy.security.xss;

import java.io.Writer;

public interface LucyXssFilter {

	String doFilter(String dirty);
	void doFilter(String dirty, Writer writer);

}
