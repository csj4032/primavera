package com.genius.primavera;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.yaml.snakeyaml.Yaml;

import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class HelloApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void yamlTest() {
		Config config = new Config();
		config.setCategories(List.of(
				new Category(1L, List.of(new Info(1L, "가"), new Info(2L, "나"))),
				new Category(2L, List.of(new Info(3L, "가"), new Info(4L, "나")))
		));
		Yaml yaml = new Yaml();
		String out = yaml.dump(config);
	}
}