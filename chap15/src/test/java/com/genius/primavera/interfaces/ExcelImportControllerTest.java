package com.genius.primavera.interfaces;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ExcelImportController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("엑셀파일 저장 테스트")
public class ExcelImportControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@Order(1)
	@DisplayName("path 확인 테스트")
	public void pathTest() throws Exception {
		mockMvc.perform(post("/save")).andDo(print())
				.andExpect(status().isCreated()).andExpect(content()
				.contentType(MediaTypes.HAL_JSON_VALUE))
				.andExpect(jsonPath("$.code").value("0"));
	}

	@Test
	@Order(2)
	@DisplayName("multipart 확인 테스트")
	public void multipartTest() throws Exception {
		mockMvc.perform(post("/save")).andDo(print())
				.andExpect(status().isCreated()).andExpect(content()
				.contentType(MediaTypes.HAL_JSON_VALUE))
				.andExpect(jsonPath("$.code").value("0"));
	}
}