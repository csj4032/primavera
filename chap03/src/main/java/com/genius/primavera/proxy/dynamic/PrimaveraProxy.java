package com.genius.primavera.proxy.dynamic;

import java.util.List;

public class PrimaveraProxy {

	private List<Class<?>> interfaces;
	private Object proxy;

	public PrimaveraProxy(List<Class<?>> interfaces, Object proxy) {
		this.interfaces = interfaces;
		this.proxy = proxy;
	}

	public Object getJdkProxy() {
		return proxy;
	}

	public boolean hasInterface(Class<?> expectedInterface) {
		return interfaces.contains(expectedInterface);
	}
}
