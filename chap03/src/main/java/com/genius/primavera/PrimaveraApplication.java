package com.genius.primavera;

import com.genius.primavera.application.PrimaveraProxy;
import com.genius.primavera.application.PrimaveraProxyImpl;
import com.genius.primavera.application.PrimaveraService;
import com.genius.primavera.proxy.dynamic.ProxyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class PrimaveraApplication {

	public static void main(String[] args) {
		//SpringApplication.run(PrimaveraApplication.class, args);
		ProxyFactory proxyFactory = new ProxyFactory(PrimaveraApplication.class.getPackage());
		PrimaveraProxy primaveraProxy = proxyFactory.getBean(PrimaveraProxy.class);
		log.info("{}", primaveraProxy);
		log.info("{}", primaveraProxy.doSomething("first"));
//		PrimaveraProxy primaveraProxyImpl = new PrimaveraProxyImpl();
//		log.info("{}", primaveraProxyImpl);
//		log.info("{}", primaveraProxyImpl.doSomething("first"));
	}
}
