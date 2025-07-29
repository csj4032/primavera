package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Winner;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WinnerMapper {

    String INSERT_SQL = "INSERT INTO WINNER (USER_ID, WINNER, CREATED_AT) VALUES (#{userId}, #{winner}, #{createdAt})";

    @Insert(value = INSERT_SQL)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = long.class)
    int save(Winner winner);

    @Select(value = "SELECT ID, USER_ID, WINNER, CREATED_AT FROM WINNER")
    List<Winner> findAll();

    @Update(value = "UPDATE WINNER SET WINNER = #{winner} WHERE ID = #{id}")
    int update(Winner winner);

    @Select(value = "SELECT ID, USER_ID, WINNER, CREATED_AT FROM WINNER WHERE ID = #{id}")
    Winner findById(int id);

    @Select(value = "SELECT ID, USER_ID, WINNER, CREATED_AT FROM WINNER WHERE ID > #{id}")
    List<Winner> findByIdGt(int id);

    @Select(value = "TRUNCATE TABLE WINNER")
    void truncate();

    @Select(value = "SELECT COUNT(*) FROM WINNER WHERE ID > #{id}")
    int findByIdGtCount(int id);

    @Select(value = "SELECT COUNT(*) FROM WINNER WHERE ID > #{id} FOR UPDATE")
    int findByIdGtCountForUpdate(int id);

    @Delete(value = "DELETE FROM WINNER WHERE ID > #{id}")
    void delete(int id);

    @Select(value = "SELECT COUNT(*) FROM WINNER")
    long count();

    @Select(value = "SELECT COUNT(*) FROM WINNER WHERE USER_ID = #{id}")
    long countByUserId(long id);
}