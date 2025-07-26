package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.List;

@Slf4j
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PrimaveraPropertiesTest {

    private String jdbcUrl = "jdbc:mysql://localhost:3306/primavera";
    private String jdbcUsername = "primavera";
    private String jdbcPassword = "primavera";
    private List<String> tables = List.of("user", "role");

    @Test
    @Order(1)
    @DisplayName("Test Primavera Configuration")
    public void configurationTest() {
        GenericXmlApplicationContext genericXmlApplicationContext = new GenericXmlApplicationContext("classpath:configuration.xml");
        PrimaveraSpringBean xmlSpringBean = genericXmlApplicationContext.getBean("xmlSpringBean", PrimaveraSpringBean.class);
        Assertions.assertEquals("xmlSpringBean", xmlSpringBean.getName());
        log.info(xmlSpringBean.getName());
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(PrimaveraConfiguration.class);
        PrimaveraSpringBean annotationSpringBean = annotationConfigApplicationContext.getBean("annotationSpringBean", PrimaveraSpringBean.class);
        Assertions.assertEquals("annotationSpringBean", annotationSpringBean.getName());
    }

    @Test
    @Order(2)
    @DisplayName("Test Primavera Properties")
    public void valueTest() {
        Assertions.assertEquals("jdbc:mysql://localhost:3306/primavera", jdbcUrl);
        Assertions.assertEquals("primavera", jdbcUsername);
        Assertions.assertEquals("primavera", jdbcPassword);
        Assertions.assertEquals(tables, List.of("user", "role"));
    }
}