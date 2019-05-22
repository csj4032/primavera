package com.genius.primavera.application.article;

import com.genius.primavera.domain.mapper.ArticleMapper;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.Content;
import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.infrastructure.security.PrimaveraUserDetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class WriteArticleServiceImpl implements WriteArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleContentMapper articleContentMapper;

    @Override
    @Transactional()
    public Article write(ArticleDto.WriteRequestArticle requestArticle) {
        PrimaveraUserDetails primaveraUserDetails = (PrimaveraUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User author = primaveraUserDetails.getUser();
        boolean isStepUpdate = requestArticle.getLevel() > 1;
        Article article = new Article();
        article.setPId(requestArticle.getPId());
        article.setReference(requestArticle.getReference());
        article.setStep(requestArticle.getStep() + 1);
        article.setLevel(requestArticle.getLevel() + 1);
        article.setAuthor(author);
        article.setSubject(requestArticle.getSubject());
        article.setStatus(requestArticle.getStatus());
        article.setRegDt(Instant.now());

        articleMapper.updateStep(requestArticle.getReference(), requestArticle.getStep());

        Content content = new Content();
        content.setArticle(article);
        content.setContents(requestArticle.getText());
        article.setContent(content);

        articleMapper.save(article);
        articleContentMapper.saveContent(content);
        return article;
    }

    @Override
    public Article findById(long id) {
        return articleMapper.findById(id);
    }
}