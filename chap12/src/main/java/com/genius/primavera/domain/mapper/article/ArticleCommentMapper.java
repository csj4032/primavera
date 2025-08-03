package com.genius.primavera.domain.mapper.article;

import com.genius.primavera.domain.model.article.Comment;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ArticleCommentMapper {

    @Insert("INSERT INTO ARTICLE_COMMENT (ARTICLE_ID, LEVEL, STEP, COMMENT, AUTHOR, STATUS, CREATED_AT) VALUES (#{article.id}, #{level}, #{step}, #{comment}, #{author.id}, #{status, typeHandler=com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler}, #{createdAt})")
    int save(Comment comment);

    @Results(id = "ARTICLE_COMMENT",
            value = {
                    @Result(property = "id", column = "ID"),
                    @Result(property = "level", column = "LEVEL"),
                    @Result(property = "step", column = "STEP"),
                    @Result(property = "author.nickname", column = "NICKNAME"),
                    @Result(property = "author.connection.imageUrl", column = "IMAGE_URL"),
                    @Result(property = "comment", column = "COMMENT"),
                    @Result(property = "createdAt", column = "CREATED_AT")
            })
    @Select("SELECT A.ID, A.LEVEL, A.STEP, A.COMMENT, B.NICKNAME, C.IMAGE_URL, A.CREATED_AT FROM ARTICLE_COMMENT AS A INNER JOIN USERS B ON A.AUTHOR = B.ID INNER JOIN USER_CONNECTION C ON B.EMAIL = C.EMAIL WHERE ARTICLE_ID = #{articleId}")
    List<Comment> findByArticleId(long articleId);
}
