package com.genius.primavera.interfaces;

import com.genius.primavera.domain.model.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ExtendWith(SpringExtension.class)
class AjaxControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @Order(1)
    public void ajaxTest() {
        String ajaxHtml = testRestTemplate.getForObject("/ajax", String.class);
        org.assertj.core.api.Assertions.assertThat(ajaxHtml).contains("Ajax");
    }

    @Test
    @Order(2)
    public void htmlTest() {
        Assertions.assertEquals(testRestTemplate.getForObject("/ajax/html", String.class), "<div>html</div>");
    }

    @Test
    @Order(3)
    public void htmlFormTest() {
        User user = testRestTemplate.getForObject("/ajax/form", User.class);
        Assertions.assertEquals(1, user.getId());
    }

    @Test
    @Order(4)
    public void formDataTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        params.put("email", "email");
        User user = testRestTemplate.getForObject("/ajax/form/data?id={id}&email={email}", User.class, params);
        Assertions.assertEquals(1, user.getId());
        Assertions.assertEquals("email", user.getEmail());
    }
}
