package com.genius.primavera.template;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TemplateExistenceTest {

    @Test
    @Order(1)
    @DisplayName("connection test connection file Endpoint verification")
    public void articleTemplatesExist() {
        assertTemplateExists("templates/article/list.html");
        assertTemplateExists("templates/article/detail.html");
        assertTemplateExists("templates/article/form.html");
    }

    @Test
    @Order(2)
    @DisplayName("test connection file Endpoint verification")
    public void commonTemplatesExist() {
        assertTemplateExists("templates/fragments/header.html");
        assertTemplateExists("templates/fragments/footer.html");
        assertTemplateExists("templates/fragments/aside.html");
        assertTemplateExists("templates/layouts/layout.html");
    }

    @Test
    @Order(3)
    @DisplayName("test connection Endpoint verification")
    public void basicTemplatesExist() {
        assertTemplateExists("templates/index.html");
        assertTemplateExists("templates/login.html");
        assertTemplateExists("templates/admin.html");
        assertTemplateExists("templates/manager.html");
    }

    private void assertTemplateExists(String templatePath) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templatePath);
        assertNotNull(inputStream, "Template file should exist: " + templatePath);
        try {
            inputStream.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}