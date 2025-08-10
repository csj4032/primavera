package com.genius.primavera.lifecycle;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmlBean {

	private void afterPropertiesSet() {
		log.info("Xml bean creation translated_text_1 translated_text_3 : init() calledtranslated_text_1");
	}

	private void destroy() {
		log.info("Xml bean creation translated_text_2 : destroy calledtranslated_text_1");
	}
}
