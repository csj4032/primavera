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
    @Transactional
    public Article write(ArticleDto.WriteRequestArticle requestArticle) {
        PrimaveraUserDetails primaveraUserDetails = (PrimaveraUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User author = primaveraUserDetails.getUser();
        Article article = new Article();
        if (requestArticle.getPId() != 0) {
            Article parentArticle = articleMapper.findByArticleId(article.getParentId());
            article.setPId(parentArticle.getId());
            article.setReference(article.getReference());
            article.setStep(parentArticle.getStep() + 1);
            article.setLevel(parentArticle.getLevel() + 1);
        }
        article.setAuthor(author);
        article.setSubject(requestArticle.getSubject());
        article.setStatus(requestArticle.getStatus());
        article.setRegDt(Instant.now());
        Content content = new Content();
        content.setArticle(article);
        content.setContents(requestArticle.getText());
        articleContentMapper.saveContent(content);
        return article;
    }
}