package com.genius.primavera;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
@ExtendWith(SpringExtension.class)
class HelloControllerTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void params() {
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8080").path("hello/params")
				.queryParam("persons.name", "A","B","C")
				.queryParam("names", "A", "B", "C")
				.queryParam("ages", 1, 2, 3)
				.queryParam("enumTypes", "ABC", "DEF", "GHI");
		URI uri = builder.build().toUri();
		log.info("url : {}", uri.toString());
		ResponseEntity<String> result = testRestTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(null, null), String.class);
		log.info(result.getBody());
	}
}