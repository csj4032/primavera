package com.genius.primavera;

import com.genius.primavera.domain.model.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

@JsonTest
@ExtendWith(SpringExtension.class)
public class HelloJsonTest {

	@Autowired
	private JacksonTester<User> json;

	@Test
	public void jsonTest() throws IOException {
		User user = User.builder().id(1l).build();
		String userString = "{\"id\": 1}";
		Assertions.assertThat(json.parseObject(userString).getId()).isEqualTo(user.getId());
		Assertions.assertThat(json.write(user)).isEqualToJson("/user.json");
		Assertions.assertThat(json.write(user)).hasJsonPathNumberValue("id");
		Assertions.assertThat(json.write(user)).extractingJsonPathNumberValue("id").isEqualTo(1);
	}
}
