package com.genius.primavera.interfaces;

import com.genius.primavera.application.ExcelImportService;
import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ExcelImportController.class)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("엑셀파일 저장 테스트")
public class ExcelImportControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ResourceLoader resourceLoader;

	@MockBean
	private ExcelImportService excelImportService;

	private Resource resource;
	private ExcelImportRequest excelImportRequest;
	private ExcelImportResponse excelImportResponse;
	private MockMultipartFile multipartFile;
	private MultiValueMap<String, String> multiValueMap;

	@Test
	@DisplayName("첨부파일 접근 테스트")
	public void getResourceTest() {
		resource = resourceLoader.getResource("classpath:20191225.txt");
		assertTrue(resource.exists());
	}

	@Nested
	@DisplayName("multipart 테스트")
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
	class Save {

		@BeforeAll
		public void setUp() throws IOException {
			multipartFile = new MockMultipartFile("file", resource.getInputStream());
			excelImportRequest = new ExcelImportRequest("20191225.txt", multipartFile);
			excelImportResponse = new ExcelImportResponse("Honda", excelImportRequest.getSize());
			multiValueMap = new LinkedMultiValueMap() {{
				add("name", "20191225.txt");
			}};
		}

		@Test
		@Order(1)
		@DisplayName("path 확인 테스트")
		public void pathTest() throws Exception {
			when(excelImportService.excelImport(new ExcelImportRequest("20191225.txt", multipartFile))).thenReturn(new ExcelImportResponse("", 100));
			mockMvc.perform(post("/save")).andDo(print())
					.andExpect(status().isCreated()).andExpect(content()
					.contentType(MediaTypes.HAL_JSON_VALUE))
					.andExpect(jsonPath("$.name").doesNotExist());
		}

		@Test
		@Order(2)
		@DisplayName("multipart 확인 테스트")
		public void multipartTest() throws Exception {
			when(excelImportService.excelImport(new ExcelImportRequest("20191225.txt", multipartFile))).thenReturn(new ExcelImportResponse("", 100));
			mockMvc.perform(
					multipart("/save")
							.file(multipartFile)
							.params(multiValueMap)
							.contentType(MediaType.MULTIPART_FORM_DATA)).andDo(print())
					.andExpect(status().isCreated()).andExpect(content()
					.contentType(MediaTypes.HAL_JSON_VALUE))
					.andExpect(jsonPath("$.name").value("20191225.txt"))
					.andExpect(jsonPath("$.size").value(multipartFile.getSize()));
		}

		@Test
		@Order(3)
		@DisplayName("excel 파일 형식 확인 테스트")
		public void isExcelFile() {
		}
	}
}