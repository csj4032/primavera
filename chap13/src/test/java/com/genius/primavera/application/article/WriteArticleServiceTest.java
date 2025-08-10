package com.genius.primavera.application.article;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.WriteType;
import com.genius.primavera.interfaces.WithMockPrimaveraUserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class WriteArticleServiceTest {

    @Container
    static MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
    }

    @Autowired
    private WriteArticleService writeArticleService;

    private WriteArticleService mockWriteArticleService;

    private static ArticleDto.WriteArticle writeRequestArticle;
    private static Article article_1;
    private static Article article_1_1;
    private static Article article_1_1_1;
    private static Article article_1_1_1_1;
    private static Article article_1_1_2;
    private static Article article_1_2;
    private static Article article_2;
    private static Article article_2_1;
    private static Article article_2_2;
    private static Article article_2_3;

    @BeforeAll
    public static void setUp() {
        writeRequestArticle = new ArticleDto.WriteArticle();
        writeRequestArticle.setPId(0);
        writeRequestArticle.setSubject("test_1");
        writeRequestArticle.setContents("test");
        writeRequestArticle.setWriteType(WriteType.FORM);
    }

    @Test
    @Order(1)
    @DisplayName("Mock should test")
    public void mockWriteTest() {
        mockWriteArticleService = Mockito.mock(WriteArticleService.class);
        Article mockArticle = new Article();
        given(this.mockWriteArticleService.save(writeRequestArticle)).willReturn(mockArticle);
        assertNotNull(mockWriteArticleService.save(writeRequestArticle));
    }

    @Test
    @Order(2)
    @DisplayName("test connection test")
    @WithMockPrimaveraUserDetails
    public void writeTest() {
        article_1 = writeArticleService.save(writeRequestArticle);
        article_1 = writeArticleService.findById(article_1.getId());
        assertEquals(null, article_1.getParent());
        assertEquals(1, article_1.getStep());
    }

    @Test
    @Order(3)
    @DisplayName("test connection should test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeFirstReplyTest() {
        Article origin = writeArticleService.findById(article_1.getId());
        writeRequestArticle.setPId(origin.getId());
        writeRequestArticle.setSubject(origin.getSubject() + "_1");
        writeRequestArticle.setContents("test connection should test");
        writeRequestArticle.setWriteType(WriteType.REPLY);
        article_1_1 = writeArticleService.save(writeRequestArticle);
        assertEquals(article_1.getReference(), article_1_1.getReference());
        assertEquals(2, article_1_1.getLevel());
        assertEquals(2, article_1_1.getStep());
    }

    @Test
    @Order(4)
    @DisplayName("test connection should test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeSecondReplyTest() {
        Article origin = writeArticleService.findById(article_1.getId());
        writeRequestArticle.setPId(origin.getId());
        writeRequestArticle.setSubject(origin.getSubject() + "_2");
        writeRequestArticle.setContents("test connection should test");
        writeRequestArticle.setWriteType(WriteType.REPLY);
        article_1_2 = writeArticleService.save(writeRequestArticle);
        assertEquals(article_1.getReference(), article_1_2.getReference());
        assertEquals(2, article_1_2.getLevel());
        assertEquals(2, article_1_2.getStep());
    }

    @Test
    @Order(5)
    @DisplayName("test connection should connection should test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeFirst_FirstReplyTest() {
        Article origin = writeArticleService.findById(article_1_1.getId());
        writeRequestArticle.setPId(origin.getId());
        writeRequestArticle.setSubject(origin.getSubject() + "_1");
        writeRequestArticle.setContents("test connection should connection should test");
        writeRequestArticle.setWriteType(WriteType.REPLY);
        article_1_1_1 = writeArticleService.save(writeRequestArticle);
        assertEquals(article_1.getReference(), article_1_1_1.getReference());
        assertEquals(3, article_1_1_1.getLevel());
        assertEquals(4, article_1_1_1.getStep());
    }

    @Test
    @Order(6)
    @DisplayName("test connection test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeSecondTest() {
        writeRequestArticle.setPId(0);
        writeRequestArticle.setSubject("test_2");
        writeRequestArticle.setContents("test");
        writeRequestArticle.setWriteType(WriteType.FORM);
        article_2 = writeArticleService.save(writeRequestArticle);
        article_2 = writeArticleService.findById(article_2.getId());
        assertEquals(null, article_1.getParent());
        assertEquals(1, article_1.getStep());
    }

    @Test
    @Order(7)
    @DisplayName("test connection should connection should connection should test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeFirst_FirstReply_FirstReplyTest() {
        Article origin = writeArticleService.findById(article_1_1_1.getId());
        writeRequestArticle.setPId(origin.getId());
        writeRequestArticle.setSubject(origin.getSubject() + "_1");
        writeRequestArticle.setContents("test connection should connection should connection should test");
        writeRequestArticle.setWriteType(WriteType.REPLY);
        article_1_1_1_1 = writeArticleService.save(writeRequestArticle);
        assertEquals(4, article_1_1_1_1.getLevel());
        assertEquals(5, article_1_1_1_1.getStep());
    }

    @Test
    @Order(8)
    @DisplayName("test connection should connection should connection should test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeFirst_FirstReply_SecondReplyTest() {
        Article origin = writeArticleService.findById(article_1_1.getId());
        writeRequestArticle.setPId(origin.getId());
        writeRequestArticle.setSubject(origin.getSubject() + "_2");
        writeRequestArticle.setContents("test connection should connection should test");
        writeRequestArticle.setWriteType(WriteType.REPLY);
        article_1_1_2 = writeArticleService.save(writeRequestArticle);
        assertEquals(article_1.getReference(), article_1_1_2.getReference());
        assertEquals(3, article_1_1_2.getLevel());
        assertEquals(4, article_1_1_2.getStep());
    }

    @Test
    @Order(9)
    @DisplayName("test connection should connection test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeSecond_FirstReply_Test() {
        Article origin = writeArticleService.findById(article_2.getId());
        writeRequestArticle.setPId(origin.getId());
        writeRequestArticle.setSubject(origin.getSubject() + "_1");
        writeRequestArticle.setContents("test connection should connection test");
        article_2_1 = writeArticleService.save(writeRequestArticle);
        assertEquals(article_2.getReference(), article_2_1.getReference());
        assertEquals(2, article_2_1.getLevel());
        assertEquals(2, article_2_1.getStep());
    }

    @Test
    @Order(10)
    @DisplayName("test connection should connection test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeSecond_SecondReply_Test() {
        Article origin = writeArticleService.findById(article_2.getId());
        writeRequestArticle.setPId(origin.getId());
        writeRequestArticle.setSubject(origin.getSubject() + "_2");
        writeRequestArticle.setContents("test connection should connection test");
        writeRequestArticle.setWriteType(WriteType.REPLY);
        article_2_2 = writeArticleService.save(writeRequestArticle);
        assertEquals(article_2.getReference(), article_2_2.getReference());
        assertEquals(2, article_2_2.getLevel());
        assertEquals(2, article_2_2.getStep());
    }

    @Test
    @Order(11)
    @DisplayName("test connection should connection test")
    @WithUserDetails(value = "genius@primavera.com", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeSecond_ThirdReply_Test() {
        Article origin = writeArticleService.findById(article_2.getId());
        writeRequestArticle.setPId(origin.getId());
        writeRequestArticle.setSubject(origin.getSubject() + "_3");
        writeRequestArticle.setWriteType(WriteType.REPLY);
        writeRequestArticle.setContents("test connection should connection test");
        article_2_3 = writeArticleService.save(writeRequestArticle);
        assertEquals(article_2.getReference(), article_2_3.getReference());
        assertEquals(2, article_2_3.getLevel());
        assertEquals(2, article_2_3.getStep());
    }
}