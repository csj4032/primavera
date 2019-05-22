package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ArticleMapper {

    @InsertProvider(type = ArticleProvider.class, method = "save")
    @Options(useGeneratedKeys = true, keyColumn = "ID", keyProperty = "id")
    int save(Article article);

    @Results(id = "ARTICLE_WITH_USER",
            value = {
                    @Result(property = "id", column = "ID"),
                    @Result(property = "pId", column = "P_ID"),
                    @Result(property = "parent", javaType = Article.class, column = "P_ID", one = @One(select = "findById", fetchType = FetchType.DEFAULT)),
                    @Result(property = "children", javaType = Article[].class, column = "ID", many = @Many(select = "findByIdForChildren", fetchType = FetchType.DEFAULT)),
                    @Result(property = "reference", column = "REFERENCE"),
                    @Result(property = "step", column = "STEP"),
                    @Result(property = "level", column = "LEVEL"),
                    @Result(property = "author.id", column = "AUTHOR"),
                    @Result(property = "author.email", column = "EMAIL"),
                    @Result(property = "author.nickname", column = "EMAIL"),
                    @Result(property = "subject", column = "SUBJECT"),
                    @Result(property = "status", typeHandler = ArticleStatusTypeHandler.class, column = "STATUS"),
                    @Result(property = "regDt", column = "REG_DT"),
                    @Result(property = "modDt", column = "MOD_DT")

            })
    @Select("SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.AUTHOR, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT FROM ARTICLE A INNER JOIN USER B ON A.AUTHOR = B.ID WHERE A.P_ID = 0")
    List<Article> findAll();

    @ResultMap(value = "ARTICLE_WITH_USER")
    @Select(value = {
            "SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP,(SELECT MAX(STEP) FROM ARTICLE WHERE REFERENCE = A.REFERENCE) AS STEP_MAX, ",
            "A.LEVEL, A.AUTHOR, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT ",
            "FROM ARTICLE A INNER JOIN USER B ON A.AUTHOR = B.ID WHERE A.ID = #{id}"
    })
    Article findById(long id);

    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "pId", column = "P_ID"),
            @Result(property = "children", javaType = Article[].class, column = "ID", many = @Many(select = "findByIdForChildren", fetchType = FetchType.DEFAULT)),
            @Result(property = "reference", column = "REFERENCE"),
            @Result(property = "step", column = "STEP"),
            @Result(property = "maxStep", column = "MAX_STEP"),
            @Result(property = "level", column = "LEVEL"),
            @Result(property = "author.id", column = "AUTHOR"),
            @Result(property = "author.email", column = "EMAIL"),
            @Result(property = "author.nickname", column = "EMAIL"),
            @Result(property = "subject", column = "SUBJECT"),
            @Result(property = "status", typeHandler = ArticleStatusTypeHandler.class, column = "STATUS"),
            @Result(property = "regDt", column = "REG_DT"),
            @Result(property = "modDt", column = "MOD_DT")

    })
    @Select("SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.AUTHOR, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.REG_DT, A.MOD_DT FROM ARTICLE A INNER JOIN USER B ON A.AUTHOR = B.ID WHERE A.P_ID = #{id}")
    Article findByIdForChildren(long id);

    @Update("UPDATE ARTICLE SET STEP = STEP + 1 WHERE REFERENCE = #{reference} AND STEP > #{step}")
    void updateStep(@Param("reference") long reference, @Param("step") int step);

    class ArticleProvider {
        public String save(Article article) {
            String sql = "INSERT INTO ARTICLE (P_ID, REFERENCE, STEP, LEVEL, SUBJECT, AUTHOR, STATUS, REG_DT) VALUES (#{pId}, ";
            if (article.getPId() == 0) {
                sql += "LAST_INSERT_ID() + 1";
            } else {
                sql += "#{reference}";
            }
            sql += ", #{step}, #{level}, #{subject}, #{author.id}, #{status, typeHandler=ArticleStatusTypeHandler}, #{regDt})";
            return sql;
        }
    }
}