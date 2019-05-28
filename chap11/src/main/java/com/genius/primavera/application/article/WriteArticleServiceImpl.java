package com.genius.primavera.application.article;

import com.genius.primavera.domain.ArticleNotFoundException;
import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.mapper.ArticleMapper;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.Content;
import com.genius.primavera.domain.model.user.User;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static com.genius.primavera.application.PrimaveraUtil.getUser;

@Service
public class WriteArticleServiceImpl implements WriteArticleService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleContentMapper articleContentMapper;

    @Override
    @Transactional
    public Article write(ArticleDto.WriteArticle writeArticle) {
        Article origin = getOriginArticle(writeArticle);
        Article article = getArticle(origin, writeArticle, getUser());
        articleMapper.updateStep(origin.getReference(), origin.getStep());
        Content content = getContent(writeArticle, article);
        article.setContent(content);
        articleMapper.save(article);
        articleContentMapper.saveContent(content);
        return article;
    }

    @Override
    public Article findById(long id) {
        return articleMapper.findById(id);
    }

    @Override
    public ArticleDto.DetailArticle findByIdWithContent(long id) {
        return modelMapper.map(articleMapper.findByIdWithContent(id), ArticleDto.DetailArticle.class);
    }

    @Override
    public Paged<ArticleDto.ListArticle> findForPageable(PageRequest pageRequest) {
        return new Paged<>(pageRequest, modelMapper.map(articleMapper.findForPageable(pageRequest), new TypeToken<List<ArticleDto.ListArticle>>() {
        }.getType()), articleMapper.findAllCount());
    }

    @Override
    public String getOriginSubject(long originId) {
        Article article = findById(originId);
        return article == null ? "" : article.getSubject();
    }

    private Article getOriginArticle(ArticleDto.WriteArticle writeArticle) {
        Article article = articleMapper.findById(writeArticle.getPId());
        if (writeArticle.getPId() != 0 && article == null) {
            throw new ArticleNotFoundException();
        }
        return article == null ? new Article() : article;
    }

    private Article getArticle(Article origin, ArticleDto.WriteArticle writeArticle, User author) {
        var article = new Article();
        article.setPId(origin.getId());
        article.setReference(origin.getReference());
        article.setStep(origin.getStep() + 1);
        article.setLevel(origin.getLevel() + 1);
        article.setAuthor(author);
        article.setSubject(writeArticle.getSubject());
        article.setStatus(writeArticle.getStatus());
        article.setRegDt(Instant.now());
        return null;
    }

    private Content getContent(ArticleDto.WriteArticle writeArticle, Article article) {
        Content content = new Content();
        content.setArticle(article);
        content.setContents(writeArticle.getContents());
        return content;
    }
}