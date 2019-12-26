package com.genius.primavera.application;

import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import com.genius.primavera.domain.MediaType;
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
@DisplayName("ExcelImportService 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExcelImportServiceImplTest {

	@Autowired
	private ExcelImportService excelImportService;

	@Autowired
	private ResourceLoader resourceLoader;

	@Test
	@Order(1)
	@DisplayName("파일 사이즈 테스트")
	public void fileSizeZeroTest() throws IOException {
		ExcelImportRequest excelImportRequest = new ExcelImportRequest();
		long size = excelImportService.getFileSize(excelImportRequest);
		Assertions.assertEquals(0, size);
		ExcelImportResponse excelImportResponse = excelImportService.excelImport(excelImportRequest);
		Assertions.assertEquals(MediaType.UNKNOWN, excelImportResponse.getMediaType());
	}

	@Test
	@Order(2)
	@DisplayName("파일 형식 텍스츠 테스트")
	public void textFileTest() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:20191225.txt");
		MockMultipartFile multipartFile = new MockMultipartFile("file", resource.getInputStream());
		ExcelImportRequest excelImportRequest = new ExcelImportRequest("20191225.txt", multipartFile);
		System.out.println(excelImportService.getMediaType(excelImportRequest));
		ExcelImportResponse excelImportResponse = excelImportService.excelImport(excelImportRequest);
		Assertions.assertEquals(MediaType.UNKNOWN, excelImportResponse.getMediaType());
	}

	@Test
	@Order(3)
	@DisplayName("파일 형식 엑셀 테스트")
	public void excelFileTest() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:20191225.xlsx");
		MockMultipartFile multipartFile = new MockMultipartFile("file", resource.getInputStream());
		ExcelImportRequest excelImportRequest = new ExcelImportRequest("20191225.xlsx", multipartFile);
		System.out.println(excelImportService.getMediaType(excelImportRequest));
		ExcelImportResponse excelImportResponse = excelImportService.excelImport(excelImportRequest);
		Assertions.assertEquals(MediaType.EXCEL_TYPE, excelImportResponse.getMediaType());
	}
}