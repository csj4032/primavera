package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Contact;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ContactMapper {

	@Insert(value = "INSERT INTO CONTACT (USER_ID, EMAIL, REG_DATE, MOD_DATE) VALUES (#{contact.userId}, #{contact.email}, #{contact.regDate}, #{contact.modDate})")
	@Options(useGeneratedKeys = true, keyProperty = "contact.id")
	@SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "contact.id", before = false, resultType = long.class)
	int save(@Param("contact") Contact contact);

	@Results(id = "CONTACT", value = {
			@Result(property = "id", column = "ID"),
			@Result(property = "userId", column = "USER_ID"),
			@Result(property = "email", column = "EMAIL"),
			@Result(property = "regDate", column = "REG_DATE"),
			@Result(property = "modDate", column = "MOD_DATE")
	})
	@Select(value = "SELECT ID, USER_ID, EMAIL, REG_DATE, MOD_DATE FROM CONTACT WHERE USER_ID = #{userId}")
	List<Contact> findByUserId(@Param("userId") long UserId);
}