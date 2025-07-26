package com.genius.primavera.lifecycle;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmlBean {

	private void afterPropertiesSet() {
		log.info("Xml bean 생성 및 초기화 : init() 호출됨");
	}

	private void destroy() {
		log.info("Xml bean 생성 소멸 : destroy 호출됨");
	}
}
