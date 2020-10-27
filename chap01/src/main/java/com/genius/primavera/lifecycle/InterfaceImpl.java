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
		log.info("Interface bean 생성 및 초기화 : init() 호출됨");
	}

	@Override
	public void destroy() throws Exception {
		log.info("Interface bean 생성 소멸 : destroy 호출됨");
	}
}