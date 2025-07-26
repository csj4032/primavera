package com.genius.primavera.interfaces;

import com.genius.primavera.domain.AbstractContainerTest;
import com.genius.primavera.domain.model.User;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Ajax 컨트롤러 테스트 - REST API 응답 검증")
class AjaxControllerTest extends AbstractContainerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    @Order(1)
    @DisplayName("Ajax HTML 페이지 요청 테스트")
    public void ajaxTest() {
        String ajaxHtml = testRestTemplate.getForObject("/ajax", String.class);
        org.assertj.core.api.Assertions.assertThat(ajaxHtml).contains("Ajax");
    }

    @Test
    @Order(2)
    @DisplayName("HTML 문자열 응답 테스트")
    public void htmlTest() {
        Assertions.assertEquals(testRestTemplate.getForObject("/ajax/html", String.class), "<div>html</div>");
    }

    @Test
    @Order(3)
    @DisplayName("JSON 객체 응답 테스트")
    public void htmlFormTest() {
        User user = testRestTemplate.getForObject("/ajax/form", User.class);
        Assertions.assertEquals(1, user.getId());
    }

    @Test
    @Order(4)
    @DisplayName("파라미터를 통한 데이터 전달 테스트")
    public void formDataTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        params.put("email", "email");
        User user = testRestTemplate.getForObject("/ajax/form/data?id={id}&email={email}", User.class, params);
        Assertions.assertEquals(1, user.getId());
        Assertions.assertEquals("email", user.getEmail());
    }
}
