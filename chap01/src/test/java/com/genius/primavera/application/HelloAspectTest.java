package com.genius.primavera.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class HelloAspectTest {

	@Autowired
	private HelloAspect helloAspect;
	@Autowired
	private HelloService helloService;

	@Test
	public void helloAspectTest() {
		helloService.getArticle(1);
		helloService.getArticle(1);
		helloService.getArticle(1);

		Assertions.assertEquals(3, helloAspect.getArticleCount(1));
	}
}