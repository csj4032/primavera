package com.genius.primavera.interfaces;

import org.hamcrest.core.IsAnything;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Disabled("Integration test using MockMvc with @SpringBootTest - requires full Spring context")
public class HelloControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName(value = "hello world")
	public void indexTest() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(IsAnything.anything("hello world")));
	}
}