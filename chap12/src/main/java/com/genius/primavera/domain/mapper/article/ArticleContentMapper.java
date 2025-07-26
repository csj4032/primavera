package com.genius.primavera.domain.mapper.article;

import com.genius.primavera.domain.model.article.Content;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ArticleContentMapper {

    @Insert("INSERT INTO ARTICLE_CONTENT (ARTICLE_ID, CONTENTS) VALUES (#{article.id}, #{contents})")
    @Options(useGeneratedKeys = true, keyColumn = "ID", keyProperty = "id")
    int saveContent(Content content);

    @Update("UPDATE ARTICLE_CONTENT SET CONTENTS = #{contents} WHERE id = #{id}")
    int update(@Param("id") long id , @Param("contents") String contents);
}
