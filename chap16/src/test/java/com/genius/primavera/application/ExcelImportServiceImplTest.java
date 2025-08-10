package com.genius.primavera.application;

import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import com.genius.primavera.domain.FileType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName("ExcelImportService test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExcelImportServiceImplTest {

	@Autowired
	private ExcelImportService excelImportService;

	@Autowired
	private ResourceLoader resourceLoader;

	@Test
	@Order(1)
	@DisplayName("translated_text_2 translated_text_3 test")
	public void fileSizeZeroTest() throws IOException {
		ExcelImportRequest excelImportRequest = new ExcelImportRequest();
		long size = excelImportService.getFileSize(excelImportRequest);
		Assertions.assertEquals(0, size);
		ExcelImportResponse excelImportResponse = excelImportService.excelImport(excelImportRequest);
		Assertions.assertEquals(FileType.UNKNOWN, excelImportResponse.getMediaType());
	}

	@Test
	@Order(2)
	@DisplayName("translated_text_2 translated_text_2 translated_text_3 test")
	public void textFileTest() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:20191225.txt");
		MockMultipartFile multipartFile = new MockMultipartFile("file", resource.getInputStream());
		ExcelImportRequest excelImportRequest = new ExcelImportRequest("20191225.txt", multipartFile);
		ExcelImportResponse excelImportResponse = excelImportService.excelImport(excelImportRequest);
		Assertions.assertEquals(FileType.UNKNOWN, excelImportResponse.getMediaType());
	}

	@Test
	@Order(3)
	@DisplayName("translated_text_2 translated_text_2 translated_text_2 test")
	public void excelFileTest() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:20191225.xlsx");
		MockMultipartFile multipartFile = new MockMultipartFile("file", resource.getInputStream());
		ExcelImportRequest excelImportRequest = new ExcelImportRequest("20191225.xlsx", multipartFile);
		ExcelImportResponse excelImportResponse = excelImportService.excelImport(excelImportRequest);
		Assertions.assertEquals(FileType.EXCEL_TYPE, excelImportResponse.getMediaType());
	}

	@Test
	@Order(4)
	@DisplayName("translated_text_2 translated_text_2 translated_text_2 translated_text_3 test")
	public void excelFileRowCount() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:20191225.xlsx");
		MockMultipartFile multipartFile = new MockMultipartFile("file", resource.getInputStream());
		ExcelImportRequest excelImportRequest = new ExcelImportRequest("20191225.xlsx", multipartFile);
		ExcelImportResponse excelImportResponse = excelImportService.excelImport(excelImportRequest);
		Assertions.assertEquals("row count : 700", excelImportResponse.getMessage());
	}
}