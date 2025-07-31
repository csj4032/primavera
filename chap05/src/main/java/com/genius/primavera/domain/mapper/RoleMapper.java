package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Role;
import com.genius.primavera.domain.typehandler.RoleTypeHandler;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper {

    String INSERT_SQL = "INSERT INTO ROLES (TYPE) VALUES (#{role.type, typeHandler=RoleTypeHandler})";

    @Insert(value = INSERT_SQL)
    @Options(useGeneratedKeys = true, keyProperty = "role.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "role.id", before = false, resultType = long.class)
    int save(@Param("role") Role role);

    List<Role> selectAll();

    @Results(id = "ROLE", value = {
            @Result(property = "id", column = "ID"),
            @Result(property = "type", column = "TYPE", typeHandler = RoleTypeHandler.class)
    })
    @Select("SELECT ID, TYPE FROM ROLES")
    Role selectAll(@Param("id") Long id);

    @Delete("DELETE FROM ROLES")
    void deleteAll();

}
