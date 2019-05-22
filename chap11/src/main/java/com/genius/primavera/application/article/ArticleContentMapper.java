package com.genius.primavera.application.article;

import com.genius.primavera.domain.model.article.Content;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ArticleContentMapper {

    @Insert("INSERT INTO ARTICLE_CONTENT (ARTICLE_ID, CONTENTS) VALUES (#{article.id}, #{contents})")
    @Options(useGeneratedKeys = true, keyColumn = "ID", keyProperty = "id")
    int saveContent(Content content);
}
