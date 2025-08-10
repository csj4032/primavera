package com.genius.primavera.lifecycle;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmlBean {

	private void afterPropertiesSet() {
		log.info("Xml bean creation should connection : init() calledshould");
	}

	private void destroy() {
		log.info("Xml bean creation test : destroy calledshould");
	}
}
