package com.genius.primavera;

import com.genius.primavera.application.SpringBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ConfigurationTest {

	@Test
	public void configuration() {
		ApplicationContext genericXmlApplicationContext = new GenericXmlApplicationContext("classpath:configuration.xml");
		SpringBean xmlSpringBean = genericXmlApplicationContext.getBean("xmlSpringBean", SpringBean.class);
		Assertions.assertEquals("xmlSpringBean", xmlSpringBean.getName());
		log.info(xmlSpringBean.getName());

		ApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(PrimaveraConfiguration.class);
		SpringBean annotationSpringBean = annotationConfigApplicationContext.getBean("annotationSpringBean", SpringBean.class);
		Assertions.assertEquals("annotationSpringBean", annotationSpringBean.getName());
	}
}