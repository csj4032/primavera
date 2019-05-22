package com.genius.primavera.application.article;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.infrastructure.security.PrimaveraUserDetailsService;
import com.genius.primavera.interfaces.WithMockPrimaveraUserDetails;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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
    private static Article article;

    @BeforeAll
    public static void setUp() {
        writeRequestArticle = new ArticleDto.WriteRequestArticle();
        writeRequestArticle.setPId(0);
        writeRequestArticle.setSubject("제목1");
        writeRequestArticle.setText("내용1");
    }

    @Test
    @Order(1)
    @DisplayName("Mock 글 쓰기")
    public void mockWriteTest() {
        given(this.mockWriteArticleService.write(writeRequestArticle)).willReturn(new Article());
        Assertions.assertEquals(1, mockWriteArticleService.write(writeRequestArticle));
    }

    @Test
    @Order(2)
    @DisplayName("글 쓰기")
    @WithMockPrimaveraUserDetails
    public void writeTest() {
        article = writeArticleService.write(writeRequestArticle);
        Assertions.assertEquals(null, article.getParent());
    }

    @Test
    @Order(3)
    @DisplayName("덧글 쓰기")
    @WithUserDetails(value = "Genius Choi", userDetailsServiceBeanName = "primaveraUserDetailsService")
    public void writeReplyTest() {
        writeRequestArticle.setPId(article.getId());
        Assertions.assertEquals(2, writeArticleService.write(writeRequestArticle));
    }
}