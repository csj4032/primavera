package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.user.User;
import com.genius.primavera.domain.model.user.UserConnection;

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

    String SELECT_FROM_USER_JOIN_CONNECTION = "SELECT A.ID, A.EMAIL, NICKNAME, PASSWORD, STATUS, B.PROVIDER, B.PROVIDER_ID, B.PROFILE_URL, B.IMAGE_URL, REG_DATE, MOD_DATE FROM USER A INNER JOIN USER_CONNECTION B ON A.EMAIL = B.EMAIL ";
    String SELECT_FROM_USER = "SELECT ID, EMAIL, NICKNAME, PASSWORD, STATUS, REG_DATE, MOD_DATE FROM USER ";
    String INSERT_SQL = "INSERT INTO USER (EMAIL, PASSWORD, NICKNAME, STATUS, REG_DATE, MOD_DATE) " +
            "VALUES (#{user.email}, #{user.password}, #{user.nickname}, #{user.status, typeHandler=UserStatusTypeHandler}, #{user.regDate}, #{user.modDate})";

    @Results(id = "USER", value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "email", column = "EMAIL"),
            @Result(property = "nickname", column = "NICKNAME"),
            @Result(property = "status", column = "STATUS"),
            @Result(property = "regDate", column = "REG_DATE"),
            @Result(property = "modDate", column = "MOD_DATE")
    })
    @Select(value = SELECT_FROM_USER + "WHERE ID = #{id}")
    User findById(@Param(value = "id") long id);

    @ResultMap(value = "USER_WITH_ROLES")
    @Select(value = SELECT_FROM_USER_JOIN_CONNECTION + "WHERE A.EMAIL = #{email}")
    User findByEmail(@Param(value = "email") String email);

    @Select(value = SELECT_FROM_USER + "WHERE ID = #{id}")
    @Results(id = "USER_WITH_ROLES", value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "email", column = "EMAIL"),
            @Result(property = "nickname", column = "NICKNAME"),
            @Result(property = "status", column = "STATUS"),
            @Result(property = "connection.provider", column = "PROVIDER"),
            @Result(property = "connection.providerId", column = "PROVIDER_ID"),
            @Result(property = "connection.profileUrl", column = "PROFILE_URL"),
            @Result(property = "connection.imageUrl", column = "IMAGE_URL"),
            @Result(property = "regDate", column = "REG_DATE"),
            @Result(property = "modDate", column = "MOD_DATE"),
            @Result(property = "roles", javaType = List.class, column = "ID", many = @Many(select = "com.genius.primavera.domain.mapper.UserRoleMapper.findByUserId"))
    })
    User findByIdWithRoles(@Param(value = "id") long id);

    @SelectProvider(type = SqlProviderAdapter.class, method = "select")
    @ResultMap("USER_WITH_ROLES")
    List<User> findByRequestUser(SelectStatementProvider selectStatement);

    @ResultMap("USER")
    @Select(value = SELECT_FROM_USER)
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

    User findBySocial(UserConnection userConnection);
}