package com.genius.primavera.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InterfaceImpl implements InitializingBean, DisposableBean {

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Interface bean creation should connection : init() calledshould");
	}

	@Override
	public void destroy() throws Exception {
		log.info("Interface bean creation test : destroy calledshould");
	}
}