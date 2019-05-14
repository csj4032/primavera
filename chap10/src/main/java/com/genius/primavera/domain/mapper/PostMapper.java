package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.post.Post;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper
public interface PostMapper {

    String INSERT_SQL = "INSERT INTO POST (WRITER_ID, SUBJECT, CONTENTS, STATUS, REG_DT) " + "VALUES (#{writer.id}, #{subject}, #{contents}, #{status, typeHandler=PostStatusTypeHandler}, #{regDt})";
    String SELECT_ALL_SQL = "SELECT A.ID, A.SUBJECT, A.CONTENTS, A.REG_DT, A.MOD_DT, A.WRITER_ID, B.EMAIL, B.NICKNAME FROM POST AS A INNER JOIN USER B ON A.WRITER_ID = B.ID";
    String SELECT_PAGEABLE_SQL = "SELECT A.ID, A.SUBJECT, A.CONTENTS, A.REG_DT, A.MOD_DT, A.WRITER_ID, B.EMAIL, B.NICKNAME FROM POST AS A INNER JOIN USER B ON A.WRITER_ID = B.ID LIMIT #{size} OFFSET #{offset}";

    @Insert(value = INSERT_SQL)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(Post post);

    @ResultMap(value = "POST_WITH_USER")
    @Select(value = SELECT_ALL_SQL)
    List<Post> findAll();

    @Results(id = "POST_WITH_USER", value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "subject", column = "SUBJECT"),
            @Result(property = "contents", column = "CONTENTS"),
            @Result(property = "writer.id", column = "WRITER_ID"),
            @Result(property = "writer.email", column = "EMAIL"),
            @Result(property = "writer.nickname", column = "NICKNAME"),
            @Result(property = "regDt", column = "REG_DT"),
            @Result(property = "modDt", column = "MOD_DT"),
    })
    @Select(value = SELECT_PAGEABLE_SQL)
    List<Post> findForPageable(Pageable pageable);
}
