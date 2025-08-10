
package com.nhncorp.lucy.security.xss;

public class XssFilterException extends RuntimeException {
	private static final long serialVersionUID = 2560642935469511816L;

	public XssFilterException(String message) {
		super(message);
	}

	public XssFilterException(Throwable cause){
		super(cause);
	}
}
