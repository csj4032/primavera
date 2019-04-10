package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.model.UserRole;
import com.genius.primavera.domain.model.UserStatus;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.MappedTypes;

import java.util.List;

@Mapper
public interface UserRoleMapper {

	@Insert(value = "INSERT INTO USER_ROLE (USER_ID, ROLE_ID) VALUES (#{userRole.userId}, #{userRole.roleId})")
	int save(@Param("userRole") UserRole userRole);

	@Results(id = "ROLE", value = {
			@Result(property = "id", column = "ID"),
			@Result(property = "type", column = "TYPE")
	})
	@Select(value = "SELECT R.ID AS ID, R.TYPE AS TYPE FROM ROLE AS R INNER JOIN USER_ROLE AS UR ON R.ID = UR.ROLE_ID WHERE USER_ID = #{userId}")
	List<Role> findByUserId(@Param("userId") long UserId);
}