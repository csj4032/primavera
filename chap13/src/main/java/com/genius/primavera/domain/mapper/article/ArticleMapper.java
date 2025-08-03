package com.genius.primavera.domain.mapper.article;

import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.model.article.Article;
import com.genius.primavera.domain.model.article.Attachment;
import com.genius.primavera.domain.model.article.Comment;

import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Mapper
@Repository
public interface ArticleMapper {

    String SELECT_WITH_USER_SQL = "SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.AUTHOR, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.HIT, A.RECOMMEND, A.DISAPPROVE, C.ID AS CONTENTS_ID, C.CONTENTS, A.CREATED_AT, A.UPDATED_AT FROM ARTICLE A INNER JOIN USERS B ON A.AUTHOR = B.ID LEFT JOIN ARTICLE_CONTENT C ON A.ID = C.ARTICLE_ID ";
    String SELECT_WITH_USER_CONTENTS_SQL = "SELECT A.ID, A.P_ID, A.REFERENCE, A.STEP, A.LEVEL, A.AUTHOR, B.EMAIL, B.NICKNAME, A.SUBJECT, A.STATUS, A.HIT, A.RECOMMEND, A.DISAPPROVE, C.ID AS CONTENTS_ID, C.CONTENTS, A.CREATED_AT, A.UPDATED_AT FROM ARTICLE A INNER JOIN USERS B ON A.AUTHOR = B.ID INNER JOIN ARTICLE_CONTENT C ON A.ID = C.ARTICLE_ID ";

    @InsertProvider(type = ArticleProvider.class, method = "save")
    @Options(useGeneratedKeys = true, keyColumn = "ID", keyProperty = "id", useCache=false)
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
                    @Result(property = "author.nickname", column = "NICKNAME"),
                    @Result(property = "subject", column = "SUBJECT"),
                    @Result(property = "status", typeHandler = com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler.class, column = "STATUS"),
                    @Result(property = "hit", column = "HIT"),
                    @Result(property = "recommend", column = "RECOMMEND"),
                    @Result(property = "disapprove", column = "DISAPPROVE"),
                    @Result(property = "content.id", column = "CONTENTS_ID"),
                    @Result(property = "content.contents", column = "CONTENTS"),
                    @Result(property = "createdAt", column = "CREATED_AT"),
                    @Result(property = "updatedAt", column = "UPDATED_AT")

            })
    @Select(SELECT_WITH_USER_SQL)
    List<Article> findAll();

    @Results(id = "ARTICLE_DETAIL_WITH_USER",
            value = {
                    @Result(property = "id", column = "ID"),
                    @Result(property = "pId", column = "P_ID"),
                    @Result(property = "reference", column = "REFERENCE"),
                    @Result(property = "step", column = "STEP"),
                    @Result(property = "level", column = "LEVEL"),
                    @Result(property = "author.id", column = "AUTHOR"),
                    @Result(property = "author.email", column = "EMAIL"),
                    @Result(property = "author.nickname", column = "NICKNAME"),
                    @Result(property = "subject", column = "SUBJECT"),
                    @Result(property = "status", typeHandler = com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler.class, column = "STATUS"),
                    @Result(property = "hit", column = "HIT"),
                    @Result(property = "recommend", column = "RECOMMEND"),
                    @Result(property = "disapprove", column = "DISAPPROVE"),
                    @Result(property = "content.id", column = "CONTENTS_ID"),
                    @Result(property = "content.contents", column = "CONTENTS"),
                    @Result(property = "createdAt", column = "CREATED_AT"),
                    @Result(property = "updatedAt", column = "UPDATED_AT")
            })
    @Select(value = SELECT_WITH_USER_SQL + " WHERE A.ID = #{id}")
    Article findById(long id);

    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "pId", column = "P_ID"),
            @Result(property = "children", javaType = Article[].class, column = "ID", many = @Many(select = "findByIdForChildren", fetchType = FetchType.DEFAULT)),
            @Result(property = "reference", column = "REFERENCE"),
            @Result(property = "step", column = "STEP"),
            @Result(property = "level", column = "LEVEL"),
            @Result(property = "author.id", column = "AUTHOR"),
            @Result(property = "author.email", column = "EMAIL"),
            @Result(property = "author.nickname", column = "NICKNAME"),
            @Result(property = "subject", column = "SUBJECT"),
            @Result(property = "status", typeHandler = com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler.class, column = "STATUS"),
            @Result(property = "hit", column = "HIT"),
            @Result(property = "recommend", column = "RECOMMEND"),
            @Result(property = "disapprove", column = "DISAPPROVE"),
            @Result(property = "content.id", column = "CONTENTS_ID"),
            @Result(property = "content.contents", column = "CONTENTS"),
            @Result(property = "createdAt", column = "CREATED_AT"),
            @Result(property = "updatedAt", column = "UPDATED_AT")
    })
    @Select(SELECT_WITH_USER_SQL + " WHERE A.P_ID = #{id}")
    Article findByIdForChildren(long id);

    @Update("UPDATE ARTICLE SET STEP = STEP + 1 WHERE REFERENCE = #{reference} AND STEP > #{step}")
    void updateStep(@Param("reference") long reference, @Param("step") int step);

    @Select("SELECT COUNT(*) AS CNT FROM ARTICLE WHERE STATUS = 1")
    int findAllCount();

    @Results(value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "pId", column = "P_ID"),
            @Result(property = "reference", column = "REFERENCE"),
            @Result(property = "step", column = "STEP"),
            @Result(property = "level", column = "LEVEL"),
            @Result(property = "author.id", column = "AUTHOR"),
            @Result(property = "author.email", column = "EMAIL"),
            @Result(property = "author.nickname", column = "NICKNAME"),
            @Result(property = "subject", column = "SUBJECT"),
            @Result(property = "status", typeHandler = com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler.class, column = "STATUS"),
            @Result(property = "hit", column = "HIT"),
            @Result(property = "recommend", column = "RECOMMEND"),
            @Result(property = "disapprove", column = "DISAPPROVE"),
            @Result(property = "content.id", column = "CONTENTS_ID"),
            @Result(property = "content.contents", column = "CONTENTS"),
            @Result(property = "createdAt", column = "CREATED_AT"),
            @Result(property = "updatedAt", column = "UPDATED_AT")
    })
    @Select(value = SELECT_WITH_USER_SQL + "WHERE A.STATUS = 1 ORDER BY A.REFERENCE DESC, A.STEP ASC LIMIT #{pageRequest.rowNumber} OFFSET #{pageRequest.offset}")
    List<Article> findForPageable(@Param("pageRequest") PageRequest pageRequest);

    @Results(id = "ARTICLE_DETAIL_WITH_USER_CONTENT",
            value = {
                    @Result(property = "id", column = "ID"),
                    @Result(property = "pId", column = "P_ID"),
                    @Result(property = "reference", column = "REFERENCE"),
                    @Result(property = "step", column = "STEP"),
                    @Result(property = "level", column = "LEVEL"),
                    @Result(property = "author.id", column = "AUTHOR"),
                    @Result(property = "author.email", column = "EMAIL"),
                    @Result(property = "author.nickname", column = "NICKNAME"),
                    @Result(property = "subject", column = "SUBJECT"),
                    @Result(property = "status", typeHandler = com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler.class, column = "STATUS"),
                    @Result(property = "hit", column = "HIT"),
                    @Result(property = "recommend", column = "RECOMMEND"),
                    @Result(property = "disapprove", column = "DISAPPROVE"),
                    @Result(property = "content.id", column = "CONTENTS_ID"),
                    @Result(property = "content.contents", column = "CONTENTS"),
                    @Result(property = "createdAt", column = "CREATED_AT"),
                    @Result(property = "updatedAt", column = "UPDATED_AT")
            })
    @Select(value = SELECT_WITH_USER_CONTENTS_SQL + " WHERE A.ID = #{id}")
    Article findByIdWithContent(long id);

    @Results(id = "ARTICLE_DETAIL_WITH_USER_CONTENT_COMMENT",
            value = {
                    @Result(property = "id", column = "ID"),
                    @Result(property = "pId", column = "P_ID"),
                    @Result(property = "reference", column = "REFERENCE"),
                    @Result(property = "step", column = "STEP"),
                    @Result(property = "level", column = "LEVEL"),
                    @Result(property = "author.id", column = "AUTHOR"),
                    @Result(property = "author.email", column = "EMAIL"),
                    @Result(property = "author.nickname", column = "NICKNAME"),
                    @Result(property = "subject", column = "SUBJECT"),
                    @Result(property = "status", typeHandler = com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler.class, column = "STATUS"),
                    @Result(property = "hit", column = "HIT"),
                    @Result(property = "recommend", column = "RECOMMEND"),
                    @Result(property = "disapprove", column = "DISAPPROVE"),
                    @Result(property = "content.id", column = "CONTENTS_ID"),
                    @Result(property = "content.contents", column = "CONTENTS"),
                    @Result(property = "comments", javaType = Comment[].class, column = "ID", many = @Many(select = "com.genius.primavera.domain.mapper.article.ArticleCommentMapper.findByArticleId", fetchType = FetchType.DEFAULT)),
                    @Result(property = "attachments", javaType = List.class, column = "ID", many = @Many(select = "com.genius.primavera.domain.mapper.article.ArticleAttachmentMapper.findByArticleId", fetchType = FetchType.DEFAULT)),
                    @Result(property = "createdAt", column = "CREATED_AT"),
                    @Result(property = "updatedAt", column = "UPDATED_AT")
            })
    @Select(value = SELECT_WITH_USER_CONTENTS_SQL + " WHERE A.ID = #{id}")
    Article findByIdWithContentAndComment(long id);

    @Update("UPDATE ARTICLE SET SUBJECT = #{subject}, STATUS = #{status, typeHandler=com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler}, UPDATED_AT = #{updatedAt} WHERE ID = #{id} ")
    int update(Article article);

    @Update("UPDATE ARTICLE SET HIT = HIT + 1 WHERE ID = #{id} ")
    int articleHit(long id);

    class ArticleProvider {
        public String save(Article article) {
            String sql = "INSERT INTO ARTICLE (P_ID, REFERENCE, STEP, LEVEL, SUBJECT, AUTHOR, STATUS, CREATED_AT) VALUES (";
            if (article.getPId() == 0l) {
                sql += "NULL, LAST_INSERT_ID() + 1";
            } else {
                sql += "#{pId}, #{reference}";
            }
            sql += ", #{step}, #{level}, #{subject}, #{author.id}, #{status, typeHandler=com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler}, #{createdAt})";
            return sql;
        }
    }
}