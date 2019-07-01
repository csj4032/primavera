package com.genius.primavera;

import com.genius.primavera.domain.model.user.User;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
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
public class PrimaveraPropertiesTest {

    @Value("${com.genius.primavera.url}")
    private String jdbcUrl;

    @Value("${com.genius.primavera.username}")
    private String jdbcUsername;

    @Value("${com.genius.primavera.password}")
    private String jdbcPassword;

    @Value("${com.genius.primavera.tables}")
    private List<String> tables;

    @Autowired
    private PrimaveraProperties primaveraProperties;

    @Test
    @Order(1)
    public void configurationTest() {
        ApplicationContext genericXmlApplicationContext = new GenericXmlApplicationContext("classpath:configuration.xml");
        PrimaveraSpringBean xmlSpringBean = genericXmlApplicationContext.getBean("xmlSpringBean", PrimaveraSpringBean.class);
        Assertions.assertEquals("xmlSpringBean", xmlSpringBean.getName());
        log.info(xmlSpringBean.getName());

        ApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(PrimaveraConfiguration.class);
        PrimaveraSpringBean annotationSpringBean = annotationConfigApplicationContext.getBean("annotationSpringBean", PrimaveraSpringBean.class);
        Assertions.assertEquals("annotationSpringBean", annotationSpringBean.getName());
    }

    @Test
    @Order(2)
    public void valueTest() {
        Assertions.assertEquals(jdbcUrl, "jdbc:mariadb://localhost:3306/primavera");
        Assertions.assertEquals(jdbcUsername, "primavera");
        Assertions.assertEquals(jdbcPassword, "primavera");
        // PR Test
        Assertions.assertEquals(tables, List.of("user", "role"));
    }

    @Test
    @Order(3)
    public void propertiesTest() {
        Assertions.assertEquals(primaveraProperties.getUrl(), "jdbc:mariadb://localhost:3306/primavera");
        Assertions.assertEquals(primaveraProperties.getUsername(), "primavera");
        Assertions.assertEquals(primaveraProperties.getPassword(), "primavera");
        // PR Test
        Assertions.assertEquals(primaveraProperties.getUsers(), List.of(User.builder().id(1l).email("genius").build(), User.builder().id(2l).email("genius2").build()));
    }
}