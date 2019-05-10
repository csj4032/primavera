package com.genius.primavera.interfaces;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@SpringBootTest
@ExtendWith({SpringExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GoogleOauthTest {

	@BeforeEach
	public void setup() {
		RestAssured.baseURI = "https://localhost";
		RestAssured.port = 8443;
	}

	@Test
	@Order(1)
	@DisplayName("로그인 페이지 접근")
	public void loginPage() throws Exception {
		given()
				.relaxedHTTPSValidation()
				.when()
				.get("/")
				.then()
				.statusCode(200)
				.contentType("text/html")
				.body(containsString("google"));
	}

	@Test
	@Order(2)
	@DisplayName("구글 로그인 시도")
	public void googleOauthTest() {
		given()
				.relaxedHTTPSValidation()
				.when()
				.redirects().follow(false)
				.get("/login/google")
				.then()
				.statusCode(302);
	}
}