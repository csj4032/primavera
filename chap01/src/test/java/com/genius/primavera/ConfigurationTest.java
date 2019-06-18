package com.genius.primavera;

import com.genius.primavera.application.MariaDB;
import com.genius.primavera.application.SpringBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ConfigurationTest {

	@Value("${mariadb.url}")
	private String jdbcUrl;

	@Value("${mariadb.username}")
	private String jdbcUsername;

	@Value("${mariadb.password}")
	private String jdbcPassword;

	@Value("${mariadb.allows}")
	private List<String> allows;

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

	@Test
	public void valueTest() {
		Assertions.assertEquals(jdbcUrl, "jdbc:mariadb://localhost:3306/primavera");
		Assertions.assertEquals(jdbcUsername, "primavera");
		Assertions.assertEquals(jdbcPassword, "primavera");
		Assertions.assertEquals(allows, List.of("local", "dev", "prod"));
	}

	@Autowired
	private MariaDB mariaDB;

	@Test
	public void propertiesTest() {
		Assertions.assertEquals(mariaDB.getUrl(), "jdbc:mariadb://localhost:3306/primavera");
		Assertions.assertEquals(mariaDB.getUsername(), "primavera");
		Assertions.assertEquals(mariaDB.getPassword(), "primavera");
		Assertions.assertEquals(mariaDB.getAllows(), List.of("local", "dev", "prod"));
	}
}