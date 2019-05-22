package com.genius.primavera.application.article;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.infrastructure.security.PrimaveraUserDetailsService;
import com.genius.primavera.interfaces.WithMockPrimaveraUserDetails;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.extern.slf4j.Slf4j;

import static org.mockito.BDDMockito.given;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WriteArticleServiceTest {

    @Autowired
    private PrimaveraUserDetailsService primaveraUserDetailsService;

    @Autowired
    private WriteArticleService writeArticleService;

    @Mock
    private WriteArticleService mockWriteArticleService;

    private static ArticleDto.WriteRequestArticle writeRequestArticle;
    private static Article article_1;
    private static Article article_1_1;
    private static Article article_1_1_1;
    private static Article article_1_2;
    private static Article article_2;

    @BeforeAll
    public static void setUp() {
        writeRequestArticle = new ArticleDto.WriteRequestArticle();
        writeRequestArticle.setPId(0);
        writeRequestArticle.setStep(0);
        writeRequestArticle.setMaxStep(0);
        writeRequestArticle.setLevel(0);
        writeRequestArticle.setSubject("제목_1");
        writeRequestArticle.setText("원글");
    }

    @Test
    @Order(1)
    @Disabled
    @DisplayName("Mock 글 쓰기")
    public void mockWriteTest() {
        given(this.mockWriteArticleService.write(writeRequestArticle)).willReturn(new Article());
        Assertions.assertEquals(new Article(), mockWriteArticleService.write(writeRequestArticle));
    }

    @Test
    @Order(2)
    @DisplayName("원글 첫번째 쓰기")
    @WithMockPrimaveraUserDetails
    public void writeTest() {
        article_1 = writeArticleService.write(writeRequestArticle);
        Assertions.assertEquals(null, article_1.getParent());
        Assertions.assertEquals(1, article_1.getStep());
    }

    @Test
    @Order(3)
    @DisplayName("원글 첫번째 덧글 쓰기")
    @WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeFirstReplyTest() {
        Article origin = writeArticleService.findById(article_1.getId());
        writeRequestArticle.setPId(origin.getId());
        writeRequestArticle.setReference(origin.getReference());
        writeRequestArticle.setStep(origin.getStep());
        writeRequestArticle.setMaxStep(origin.getMaxStep());
        writeRequestArticle.setLevel(origin.getLevel());
        writeRequestArticle.setSubject(origin.getSubject() + "_1");
        writeRequestArticle.setText("원글 첫번째 덧글 쓰기");
        article_1_1 = writeArticleService.write(writeRequestArticle);
        Assertions.assertEquals(1, article_1_1.getReference());
        Assertions.assertEquals(2, article_1_1.getLevel());
        Assertions.assertEquals(2, article_1_1.getStep());
    }

    @Test
    @Order(4)
    @DisplayName("원글 두번째 덧글 쓰기")
    @WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeSecondReplyTest() {
        Article origin = writeArticleService.findById(article_1.getId());
        writeRequestArticle.setPId(origin.getId());
        writeRequestArticle.setReference(origin.getReference());
        writeRequestArticle.setStep(origin.getStep());
        writeRequestArticle.setMaxStep(origin.getMaxStep());
        writeRequestArticle.setLevel(origin.getLevel());
        writeRequestArticle.setSubject(origin.getSubject() + "_2");
        writeRequestArticle.setText("원글 두번째 덧글 쓰기");
        article_1_2 = writeArticleService.write(writeRequestArticle);
        Assertions.assertEquals(1, article_1_2.getReference());
        Assertions.assertEquals(2, article_1_2.getLevel());
        Assertions.assertEquals(3, article_1_2.getStep());
    }

    @Test
    @Order(5)
    @DisplayName("원글 첫번째 덧글 첫번째 덧글 쓰기")
    @WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeFirst_FirstReplyTest() {
        Article origin = writeArticleService.findById(article_1_1.getId());
        writeRequestArticle.setPId(origin.getId());
        writeRequestArticle.setReference(origin.getReference());
        writeRequestArticle.setStep(origin.getStep());
        writeRequestArticle.setMaxStep(origin.getMaxStep());
        writeRequestArticle.setLevel(origin.getLevel());
        writeRequestArticle.setSubject(origin.getSubject() + "_1");
        writeRequestArticle.setText("원글 첫번째 덧글 첫번째 덧글 쓰기");
        article_1_1_1 = writeArticleService.write(writeRequestArticle);
        Assertions.assertEquals(1, article_1_1_1.getReference());
        Assertions.assertEquals(3, article_1_1_1.getLevel());
        Assertions.assertEquals(3, article_1_1_1.getStep());
        Assertions.assertEquals(3, article_1_2.getStep());
    }

    @Test
    @Order(6)
    @DisplayName("원글 두번째 덧글 첫번째 덧글 쓰기")
    @WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeSecondTest() {
        writeRequestArticle.setPId(0);
        writeRequestArticle.setStep(0);
        writeRequestArticle.setMaxStep(0);
        writeRequestArticle.setLevel(0);
        writeRequestArticle.setSubject("제목_2");
        writeRequestArticle.setText("원글");
        article_2 = writeArticleService.write(writeRequestArticle);
        Assertions.assertEquals(null, article_1.getParent());
        Assertions.assertEquals(1, article_1.getStep());
    }
}