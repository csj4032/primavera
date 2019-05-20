package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.PageRequest;
import com.genius.primavera.domain.model.post.Post;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.type.InstantTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.time.Instant;
import java.util.List;

@Mapper
public interface PostMapper {

    String INSERT_SQL = "INSERT INTO POST (WRITER_ID, SUBJECT, CONTENTS, STATUS, REG_DT) " + "VALUES (#{writer.id}, #{subject}, #{contents}, #{status, typeHandler=PostStatusTypeHandler}, #{regDt})";
    String SELECT_SQL = "SELECT A.ID, A.SUBJECT, A.CONTENTS, A.REG_DT, A.MOD_DT, A.WRITER_ID, B.EMAIL, B.NICKNAME FROM POST AS A INNER JOIN USER B ON A.WRITER_ID = B.ID";
    String SELECT_COUNT_SQL = "SELECT COUNT(*) AS CNT FROM POST AS A INNER JOIN USER B ON A.WRITER_ID = B.ID";

    @Insert(value = INSERT_SQL)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(Post post);

    @ResultMap(value = "POST_WITH_USER")
    @Select(value = SELECT_SQL)
    List<Post> findAll();

    @Select(value = SELECT_COUNT_SQL)
    int findAllCount();

    @Results(id = "POST_WITH_USER", value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "subject", column = "SUBJECT"),
            @Result(property = "contents", column = "CONTENTS"),
            @Result(property = "writer.id", column = "WRITER_ID"),
            @Result(property = "writer.email", column = "EMAIL"),
            @Result(property = "writer.nickname", column = "NICKNAME"),
            @Result(property = "regDt", javaType = Instant.class, typeHandler = InstantTypeHandler.class, column = "REG_DT", jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
            @Result(property = "modDt", javaType = Instant.class, typeHandler = InstantTypeHandler.class, column = "MOD_DT", jdbcType = JdbcType.TIMESTAMP_WITH_TIMEZONE),
    })
    @Select(value = SELECT_SQL + " ORDER BY A.ID DESC LIMIT #{pageRequest.rowNumber} OFFSET #{pageRequest.offset}")
    List<Post> findForPageable(@Param("pageRequest") PageRequest pageRequest, @Param("keyword") String keyword);

    @ResultMap(value = "POST_WITH_USER")
    @Select(value = SELECT_SQL + " WHERE A.ID = #{id} ORDER BY A.ID DESC")
    Post findById(long id);
}
