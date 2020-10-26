package com.genius.primavera.proxy.cglib;

import com.genius.primavera.application.DoSomethingImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.NoOp;

@Slf4j
public class EnhancerTest {

	@Test
	public void enhancerTest() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(DoSomethingImpl.class);
		enhancer.setCallback(NoOp.INSTANCE);
		DoSomethingImpl doSomething = (DoSomethingImpl) enhancer.create();
		log.info("{}", doSomething);
		String result = doSomething.doSomething("first");
		log.info("{}", result);
		DoSomethingImpl doSomething1 = new DoSomethingImpl();
		log.info("{}", doSomething1);
	}
}
