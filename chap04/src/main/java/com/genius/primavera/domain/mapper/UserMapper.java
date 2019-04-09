package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.User;
import org.apache.ibatis.annotations.*;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

import java.util.List;

@Mapper
public interface UserMapper {

	String SELECT_ID_NAME_REG_DATE_MOD_DATE_FROM_USER = "SELECT ID, NAME, REG_DATE, MOD_DATE FROM USER ";

	@Results(id = "USER", value = {
			@Result(property = "id", column = "ID"),
			@Result(property = "name", column = "NAME"),
			@Result(property = "regDate", column = "REG_DATE"),
			@Result(property = "modDate", column = "MOD_DATE")
	})
	@Select(value = SELECT_ID_NAME_REG_DATE_MOD_DATE_FROM_USER + "WHERE ID = #{id}")
	User findById(@Param(value = "id") long id);

	@Select(value = SELECT_ID_NAME_REG_DATE_MOD_DATE_FROM_USER + "WHERE ID = #{id}")
	@Results(id = "USER_WITH_CONTACTS", value = {
			@Result(property = "id", column = "ID"),
			@Result(property = "name", column = "NAME"),
			@Result(property = "regDate", column = "REG_DATE"),
			@Result(property = "modDate", column = "MOD_DATE"),
			@Result(property = "contacts", javaType = List.class, column = "ID", many = @Many(select = "com.genius.primavera.domain.mapper.ContactMapper.findByUserId"))
	})
	User findByIdWithContacts(@Param(value = "id") long id);

	@SelectProvider(type= SqlProviderAdapter.class, method="select")
	@ResultMap("USER_WITH_CONTACTS")
	List<User> findByRequestUser(SelectStatementProvider selectStatement);

	@ResultMap("USER")
	@Select(value = SELECT_ID_NAME_REG_DATE_MOD_DATE_FROM_USER)
	List<User> findAll();

	@Insert(value = "INSERT INTO USER (NAME, REG_DATE, MOD_DATE) VALUES (#{user.name}, #{user.regDate}, #{user.modDate})")
	@Options(useGeneratedKeys = true, keyProperty = "user.id")
	@SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "user.id", before = false, resultType = long.class)
	int save(@Param("user") User user);

	@Update(value = "UPDATE USER SET NAME = #{user.name}, MOD_DATE = #{user.modDate} WHERE ID = #{user.id}")
	int update(@Param("user") User user);

	@Delete(value = "DELETE FROM USER")
	int deleteAll();

	@Delete(value = "DELETE FROM USER WHERE ID = #{id}")
	int deleteById(@Param(value = "id") long id);
}