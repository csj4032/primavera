package com.genius.primavera.application;

import com.genius.primavera.proxy.dynamic.ProxyAnnotation;
import com.genius.primavera.proxy.dynamic.ProxyPointAnnotation;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
@ProxyAnnotation
public class DoSomethingImpl implements DoSomething {

	@Override
	@ProxyPointAnnotation
	public String doSomething(String first) {
		log.info("{} ", this);
		return doSomething(first, "second");
	}

	@Override
	@ProxyPointAnnotation
	public String doSomething(String first, String second) {
		log.info("{} ", this);
		return first + " " + second;
	}

	@Override
	public String doSomething(String first, String second, String third) {
		return null;
	}
}