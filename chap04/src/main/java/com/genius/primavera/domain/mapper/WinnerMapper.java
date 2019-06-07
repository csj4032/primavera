package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Winner;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectKey;

@Mapper
public interface WinnerMapper {

	String INSERT_SQL = "INSERT INTO WINNER (USER_ID, WINNER, REG_DT) " + "VALUES (#{userId}, #{winner}, #{regDt})";

	@Insert(value = INSERT_SQL)
	@Options(useGeneratedKeys = true, keyProperty = "id")
	@SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = long.class)
	int insertWinner(Winner winner);
}
