package com.genius.primavera.interfaces;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Order(1)
	@DisplayName("connection registration test")
	@WithUserDetails(value = "csj4032@gmail.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void form() throws Exception {
		mockMvc.perform(get("/posts/form")).andExpect(status().isOk());
	}

	@Test
	@Order(2)
	@DisplayName("connection test should test")
	@WithUserDetails(value = "csj4032@gmail.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void save() throws Exception {
		MultiValueMap params = new LinkedMultiValueMap();
		params.set("subject", "connection test");
		params.set("contents", "file exists Endpoint connection file connection test <connection> should connection.");
		params.set("writer", "1");
		mockMvc.perform(post("/posts/save").params(params)).andExpect(status().is3xxRedirection());
	}

	@Test
	@Order(3)
	@DisplayName("connection test test")
	@WithUserDetails(value = "csj4032@gmail.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void detail() throws Exception {
		mockMvc.perform(get("/posts/1").accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Endpoint")))
				.andExpect(content().string(containsString("with")))
				.andExpect(content().string(containsString("Genius")));
	}

	@Test
	@Order(4)
	@DisplayName("connection test")
	@WithUserDetails(value = "csj4032@gmail.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
	public void listForPageable() throws Exception {
		mockMvc.perform(get("/posts")
				.param("page", "1")
				.param("size", "10")
				.param("keyword", "connection")
				.accept(MediaType.TEXT_HTML))
				.andExpect(status().isOk());
	}
}