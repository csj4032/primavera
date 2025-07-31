package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.model.UserStatus;
import org.apache.ibatis.annotations.*;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.util.SqlProviderAdapter;

import java.util.List;

@Mapper
public interface UserMapper {

    String SELECT_ID_NAME_CREATED_AT_UPDATED_AT_FROM_USER = """
            SELECT 
            	ID, EMAIL, NICKNAME, PASSWORD, STATUS, CREATED_AT, UPDATED_AT 
            FROM 
            	USERS
            """;
    String INSERT_SQL = """
            INSERT INTO USERS
            	(EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT, UPDATED_AT) 
            VALUES 
            	(#{user.email}, #{user.password}, #{user.nickname}, #{user.status, typeHandler=UserStatusTypeHandler}, #{user.createdAt}, #{user.updatedAt})
            """;

    @Results(id = "USER", value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "name", column = "NAME"),
            @Result(property = "createdAt", column = "CREATED_AT"),
            @Result(property = "updatedAt", column = "UPDATED_AT")
    })
    @Select(value = SELECT_ID_NAME_CREATED_AT_UPDATED_AT_FROM_USER + "WHERE ID = #{id}")
    User findById(@Param(value = "id") long id);

    @Select(value = SELECT_ID_NAME_CREATED_AT_UPDATED_AT_FROM_USER + "WHERE ID = #{id}")
    @Results(id = "USER_WITH_ROLES", value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "email", column = "EMAIL"),
            @Result(property = "password", column = "PASSWORD"),
            @Result(property = "createdAt", column = "CREATED_AT"),
            @Result(property = "updatedAt", column = "UPDATED_AT"),
            @Result(property = "roles", javaType = List.class, column = "ID", many = @Many(select = "com.genius.primavera.domain.mapper.UserRoleMapper.findByUserId"))
    })
    User findByIdWithRoles(@Param(value = "id") long id);

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @ResultMap("USER_WITH_ROLES")
    List<User> findByRequestUser(SelectStatementProvider selectStatement);

    @ResultMap("USER")
    @Select(value = SELECT_ID_NAME_CREATED_AT_UPDATED_AT_FROM_USER)
    List<User> findAll();

    @Insert(value = INSERT_SQL)
    @Options(useGeneratedKeys = true, keyProperty = "user.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "user.id", before = false, resultType = Long.class)
    Long save(@Param("user") User user);

    @Insert(value = {"""
            <script>
            	INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME, STATUS, CREATED_AT, UPDATED_AT) 
            	VALUES 
            	<foreach collection='users' item='user' separator=','> 
            	(#{user.email}, #{user.password}, #{user.nickname}, #{user.status, typeHandler=UserStatusTypeHandler}, #{user.createdAt}, #{user.updatedAt}) 
            	</foreach> 
            </script>
            """
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int saveAll(@Param("users") List<User> users);

    @Update(value = "UPDATE USERS SET NICKNAME = #{user.nickname}, STATUS = #{user.status, typeHandler=UserStatusTypeHandler}, UPDATED_AT = #{user.updatedAt} WHERE ID = #{user.id}")
    int update(@Param("user") User user);

    @Delete(value = "DELETE FROM USERS")
    int deleteAll();

    @Delete(value = "DELETE FROM USERS WHERE ID = #{id}")
    int deleteById(@Param(value = "id") long id);

    @Select("SELECT COUNT(*) FROM USERS")
    long count();

    @Select("SELECT COUNT(*) FROM USERS WHERE EMAIL = #{email}")
    long countByEmail(String email);

    @Select("SELECT COUNT(*) FROM USERS WHERE STATUS = #{userStatus, typeHandler=UserStatusTypeHandler}")
    List<User> findByStatus(UserStatus userStatus);
}