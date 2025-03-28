package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.User;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

import java.util.List;

@Mapper
public interface UserMapper {

	String SELECT_ID_NAME_REG_DATE_MOD_DATE_FROM_USER = "SELECT ID, EMAIL, NICKNAME, PASSWORD, STATUS, REG_DT, MOD_DT FROM USER ";
	String INSERT_SQL = "INSERT INTO USER (EMAIL, PASSWORD, NICKNAME, STATUS, REG_DT, MOD_DT) " +
			"VALUES (#{user.email}, #{user.password}, #{user.nickname}, #{user.status, typeHandler=UserStatusTypeHandler}, #{user.regDate}, #{user.modDate})";

	@Results(id = "USER", value = {
			@Result(property = "id", column = "ID"),
			@Result(property = "name", column = "NAME"),
			@Result(property = "regDate", column = "REG_DATE"),
			@Result(property = "modDate", column = "MOD_DATE")
	})
	@Select(value = SELECT_ID_NAME_REG_DATE_MOD_DATE_FROM_USER + "WHERE ID = #{id}")
	User findById(@Param(value = "id") long id);

	@Select(value = SELECT_ID_NAME_REG_DATE_MOD_DATE_FROM_USER + "WHERE ID = #{id}")
	@Results(id = "USER_WITH_ROLES", value = {
			@Result(property = "id", column = "ID"),
			@Result(property = "email", column = "EMAIL"),
			@Result(property = "password", column = "PASSWORD"),
			@Result(property = "regDate", column = "REG_DT"),
			@Result(property = "modDate", column = "MOD_DT"),
			@Result(property = "roles", javaType = List.class, column = "ID", many = @Many(select = "com.genius.primavera.domain.mapper.UserRoleMapper.findByUserId"))
	})
	User findByIdWithRoles(@Param(value = "id") long id);

	@SelectProvider(type = SqlProviderAdapter.class, method = "select")
	@ResultMap("USER_WITH_ROLES")
	List<User> findByRequestUser(SelectStatementProvider selectStatement);

	@ResultMap("USER")
	@Select(value = SELECT_ID_NAME_REG_DATE_MOD_DATE_FROM_USER)
	List<User> findAll();

	@Insert(value = INSERT_SQL)
	@Options(useGeneratedKeys = true, keyProperty = "user.id")
	@SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "user.id", before = false, resultType = long.class)
	int save(@Param("user") User user);

	@Update(value = "UPDATE USER SET NICKNAME = #{user.nickname}, MOD_DATE = #{user.modDate} WHERE ID = #{user.id}")
	int update(@Param("user") User user);

	@Delete(value = "DELETE FROM USER")
	int deleteAll();

	@Delete(value = "DELETE FROM USER WHERE ID = #{id}")
	int deleteById(@Param(value = "id") long id);
}