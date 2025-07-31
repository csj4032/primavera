package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Winner;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WinnerMapper {

    String INSERT_SQL = "INSERT INTO WINNERS (NAME, YEAR, SPORT, PRIZE, AMOUNT) VALUES (#{name}, #{year}, #{sport}, #{prize}, #{amount})";

    @Insert(value = INSERT_SQL)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = long.class)
    int save(Winner winner);

    @Select(value = "SELECT ID, NAME, YEAR, SPORT, PRIZE, AMOUNT FROM WINNERS")
    List<Winner> findAll();

    @Update(value = "UPDATE WINNERS SET NAME = #{name}, YEAR = #{year}, SPORT = #{sport}, PRIZE = #{prize}, AMOUNT = #{amount} WHERE ID = #{id}")
    int update(Winner winner);

    @Select(value = "SELECT ID, NAME, YEAR, SPORT, PRIZE, AMOUNT FROM WINNERS WHERE ID = #{id}")
    Winner findById(Long id);

    @Select(value = "SELECT ID, NAME, YEAR, SPORT, PRIZE, AMOUNT FROM WINNERS WHERE ID > #{id}")
    List<Winner> findByIdGt(Long id);

    @Select(value = "TRUNCATE TABLE WINNERS")
    void truncate();

    @Select(value = "SELECT COUNT(*) FROM WINNERS WHERE ID > #{id}")
    int findByIdGtCount(Long id);

    @Select(value = "SELECT COUNT(*) FROM WINNERS WHERE ID > #{id} FOR UPDATE")
    int findByIdGtCountForUpdate(Long id);

    @Delete(value = "DELETE FROM WINNERS WHERE ID > #{id}")
    void delete(Long id);

    @Select(value = "SELECT COUNT(*) FROM WINNERS")
    long count();

    @Select(value = "SELECT COUNT(*) FROM WINNERS WHERE NAME = #{name}")
    long countByName(String name);

    @Insert({
            "<script>",
            "INSERT INTO WINNERS (NAME, YEAR, SPORT, PRIZE, AMOUNT) VALUES ",
            "<foreach collection='winners' item='winner' separator=','>",
            "(#{winner.name}, #{winner.year}, #{winner.sport}, #{winner.prize}, #{winner.amount})",
            "</foreach>",
            "</script>"
    })
    int bulkInsert(List<Winner> winners);

}