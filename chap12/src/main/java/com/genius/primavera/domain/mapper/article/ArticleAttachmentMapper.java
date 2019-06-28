package com.genius.primavera.domain.mapper.article;

import com.genius.primavera.domain.model.article.Attachment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ArticleAttachmentMapper {

    @Insert(value = "INSERT INTO ARTICLE_ATTACHMENT (ARTICLE_ID, NAME, PATH, SIZE) VALUES (#{article.id}, #{name}, #{path}, #{size})")
    @Options(useGeneratedKeys = true, keyColumn = "ID", keyProperty = "id")
    int save(Attachment attachments);
}
