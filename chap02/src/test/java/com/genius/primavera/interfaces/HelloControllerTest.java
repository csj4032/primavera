package com.genius.primavera.interfaces;

import com.genius.primavera.applicaiton.HelloService;
import com.genius.primavera.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HelloController.class)
public class HelloControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private HelloService helloService;

	@Test
	public void helloTest() throws Exception {
		User user = new User();
		given(helloService.getUsers()).willReturn(Collections.singletonList(user));
		mockMvc.perform(get("/hello"))
				.andExpect(status().isOk())
				.andExpect(view().name("hello"))
				.andExpect(model().attributeExists("hello"))
				.andExpect(model().attribute("hello", contains(user)));
	}
}