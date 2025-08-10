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
		log.info("Interface bean creation translated_text_1 translated_text_3 : init() calledtranslated_text_1");
	}

	@Override
	public void destroy() throws Exception {
		log.info("Interface bean creation translated_text_2 : destroy calledtranslated_text_1");
	}
}