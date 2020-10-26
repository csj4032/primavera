package com.genius.primavera.proxy.dynamic;

import com.genius.primavera.PrimaveraApplication;
import com.genius.primavera.application.DoSomething;
import com.genius.primavera.application.DoSomethingImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ProxyFactoryTest {

	@Test
	public void testProxyFactory() {
		ProxyFactory proxyFactory = new ProxyFactory(PrimaveraApplication.class.getPackage());
		DoSomething primaveraProxy = proxyFactory.getBean(DoSomething.class);
		log.info("Proxy : {}", primaveraProxy);
		log.info("Proxy DoSomething : {}", primaveraProxy.doSomething("first"));
		log.info("Proxy DoSomething2 : {}", primaveraProxy.doSomething("first2"));
		DoSomethingImpl doSomethingImpl = new DoSomethingImpl();
		log.info("{}", doSomethingImpl);
		log.info("{}", doSomethingImpl.doSomething("first"));
	}
}