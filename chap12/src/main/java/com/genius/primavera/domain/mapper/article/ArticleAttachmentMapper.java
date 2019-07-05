package com.genius.primavera.domain.mapper.article;

import com.genius.primavera.domain.model.article.Attachment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

import lombok.Setter;

@Mapper
@Repository
public interface ArticleAttachmentMapper {

    @Insert(value = "INSERT INTO ARTICLE_ATTACHMENT (ARTICLE_ID, NAME, PATH, SIZE) VALUES (#{article.id}, #{name}, #{path}, #{size})")
    @Options(useGeneratedKeys = true, keyColumn = "ID", keyProperty = "id")
    int save(Attachment attachments);

    @Select(value = "SELECT ID, ARTICLE_ID, NAME, PATH, SIZE FROM ARTICLE_ATTACHMENT WHERE ARTICLE_ID = #{articleId}")
    List<Attachment> findByArticleId(long articleId);

    @Select(value = "SELECT ID, ARTICLE_ID, NAME, PATH, SIZE FROM ARTICLE_ATTACHMENT WHERE ID = #{id}")
    Attachment findById(long id);
}
