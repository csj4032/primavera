package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.post.Post;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;

@Mapper
public interface PostMapper {

    String INSERT_SQL = "INSERT INTO POST (WRITER_ID, SUBJECT, CONTENTS, STATUS, REG_DT) " + "VALUES (#{writer.id}, #{subject}, #{contents}, #{status, typeHandler=PostStatusTypeHandler}, #{regDt})";

    @Insert(value = INSERT_SQL)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(Post post);

    List<Post> findAll();
}
