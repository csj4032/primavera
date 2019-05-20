package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ArticleMapper {

    @Insert("INSERT INTO ARTICLE (P_ID, REFERENCE, STEP, LEVEL, SUBJECT, WRITER_ID, STATUS) VALUES (#{pId}, #{reference}, #{step}, #{level}, #{subject}, #{writer.id}, #{status, typeHandler=ArticleStatusTypeHandler})")
    @Options(useGeneratedKeys = true, keyColumn = "ID", keyProperty = "id")
    int save(Article article);

    @Results(id="ARTICLE_WITH_USER",
        value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "pId", column = "P_ID"),
            @Result(property = "parent", javaType = Article.class, column = "P_ID", one = @One(select = "findByArticleId", fetchType = FetchType.DEFAULT)),
            @Result(property = "children", javaType = Article[].class, column = "ID", many = @Many(select = "findByArticleIdForChildren", fetchType = FetchType.DEFAULT)),
            @Result(property = "reference", column = "REFERENCE"),
            @Result(property = "step", column = "STEP"),
            @Result(property = "level", column = "LEVEL"),
            @Result(property = "writer.id", column = "WRITER_ID"),
            @Result(property = "writer.email", column = "EMAIL"),
            @Result(property = "writer.nickname", column = "EMAIL"),
            @Result(property = "subject", column = "SUBJECT"),
            @Result(property = "status", typeHandler = ArticleStatusTypeHandler.class, column = "STATUS"),
            @Result(property = "regDt", column = "REG_DT"),
            @Result(property = "modDt", column = "MOD_DT")

    })
    @Select("SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.WRITER_ID, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT FROM ARTICLE A INNER JOIN USER B ON A.WRITER_ID = B.ID")
    List<Article> findAll();

    @ResultMap(value = "ARTICLE_WITH_USER")
    @Select("SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.WRITER_ID, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT FROM ARTICLE A INNER JOIN USER B ON A.WRITER_ID = B.ID WHERE A.ID = #{id}")
    Article findByArticleId(long id);

    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "pId", column = "P_ID"),
            @Result(property = "reference", column = "REFERENCE"),
            @Result(property = "step", column = "STEP"),
            @Result(property = "level", column = "LEVEL"),
            @Result(property = "writer.id", column = "WRITER_ID"),
            @Result(property = "writer.email", column = "EMAIL"),
            @Result(property = "writer.nickname", column = "EMAIL"),
            @Result(property = "subject", column = "SUBJECT"),
            @Result(property = "status", typeHandler = ArticleStatusTypeHandler.class, column = "STATUS"),
            @Result(property = "regDt", column = "REG_DT"),
            @Result(property = "modDt", column = "MOD_DT")

    })
    @Select("SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.WRITER_ID, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT FROM ARTICLE A INNER JOIN USER B ON A.WRITER_ID = B.ID WHERE A.P_ID = #{id}")
    Article findByArticleIdForChildren(long id);
}