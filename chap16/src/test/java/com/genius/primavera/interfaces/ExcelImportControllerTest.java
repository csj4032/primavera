package com.genius.primavera.interfaces;

import com.genius.primavera.application.ExcelImportService;
import com.genius.primavera.domain.ExcelImportRequest;
import com.genius.primavera.domain.ExcelImportResponse;
import com.genius.primavera.domain.FileType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@DisplayName("translated_text_4 translated_text_2 test")
@WebMvcTest(ExcelImportController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
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
    @DisplayName("translated_text_4 translated_text_2 test")
    public void getResourceTest() {
        resource = resourceLoader.getResource("classpath:./data/20191225.txt");
        assertTrue(resource.exists());
    }

    @BeforeAll
    public void setUp() throws IOException {
        resource = resourceLoader.getResource("classpath:./data/20191225.xlsx");
        multipartFile = new MockMultipartFile("file", "20191225.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", resource.getInputStream());
        excelImportRequest = new ExcelImportRequest("20191225.xlsx", multipartFile);
        excelImportResponse = new ExcelImportResponse("Honda", excelImportRequest.getSize(), FileType.EXCEL_TYPE, "Success");
        multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("name", "20191225.xlsx");
    }

    @Test
    @Order(1)
    @DisplayName("path verification test")
    public void pathTest() throws Exception {
        when(excelImportService.excelImport(any(ExcelImportRequest.class))).thenReturn(new ExcelImportResponse("", 100, FileType.EXCEL_TYPE, ""));
        mockMvc.perform(multipart("/save").file(multipartFile).params(multiValueMap).contentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/hal+json"));
    }

    @Test
    @Order(2)
    @DisplayName("multipart verification test")
    public void multipartTest() throws Exception {
        when(excelImportService.excelImport(any(ExcelImportRequest.class))).thenReturn(new ExcelImportResponse("20191225.xlsx", multipartFile.getSize(), FileType.EXCEL_TYPE, ""));
        mockMvc.perform(multipart("/save").file(multipartFile).params(multiValueMap).contentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.name").value("20191225.xlsx"))
                .andExpect(jsonPath("$.size").value(multipartFile.getSize()));
    }

    @Test
    @Order(3)
    @DisplayName("excel translated_text_2 translated_text_2 verification test")
    public void isExcelFile() {

    }
}
