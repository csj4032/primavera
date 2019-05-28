package com.genius.primavera.application.article;

import com.genius.primavera.domain.ArticleNotFoundException;
import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.Paged;
import com.genius.primavera.domain.mapper.ArticleMapper;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.ArticleDto;
import com.genius.primavera.domain.model.article.Content;
import com.genius.primavera.domain.model.article.WriteType;
import com.genius.primavera.domain.model.user.User;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

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
    public Article modify(ArticleDto.WriteArticle writeArticle) {
        Article article = articleMapper.findByIdWithContent(writeArticle.getId());
        article.setSubject(writeArticle.getSubject());
        article.setModDt(Instant.now());
        articleMapper.update(article);
        articleContentMapper.update(article.getContentsId(), writeArticle.getContents());
        return article;
    }

    @Override
    public ArticleDto.FormArticle findByForForm(WriteType type, long id) {
        if (type.equals(WriteType.REPLY)) {
            return getReplayForm(id);
        } else if (type.equals(WriteType.MODIFY)) {
            return getModifyForm(id);
        }
        return getEmpltyForm();
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
        if (writeArticle.getWriteType().equals(WriteType.REPLY)) {
            Article article = articleMapper.findById(writeArticle.getPId());
            if (Objects.isNull(article)) throw new ArticleNotFoundException();
            return article;
        }
        return new Article();
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
        return article;
    }

    private Content getContent(ArticleDto.WriteArticle writeArticle, Article article) {
        Content content = new Content();
        content.setArticle(article);
        content.setContents(writeArticle.getContents());
        return content;
    }

    private ArticleDto.FormArticle getEmpltyForm() {
        return new ArticleDto.FormArticle();
    }

    private ArticleDto.FormArticle getModifyForm(long id) {
        ArticleDto.FormArticle formArticle = new ArticleDto.FormArticle();
        Article article = articleMapper.findByIdWithContent(id);
        formArticle.setId(id);
        formArticle.setSubject(article.getSubject());
        formArticle.setContents(article.getContents());
        formArticle.setWriteType(WriteType.MODIFY);
        return formArticle;
    }

    private ArticleDto.FormArticle getReplayForm(long id) {
        ArticleDto.FormArticle formArticle = new ArticleDto.FormArticle();
        Article article = articleMapper.findById(id);
        formArticle.setPId(id);
        formArticle.setSubject("[RE] " + article.getSubject());
        formArticle.setWriteType(WriteType.REPLY);
        return formArticle;
    }
}