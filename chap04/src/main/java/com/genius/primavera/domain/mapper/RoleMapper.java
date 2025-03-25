package com.genius.primavera.domain.mapper;

import com.genius.primavera.domain.model.Role;
import org.apache.ibatis.annotations.*;

@Mapper
public interface RoleMapper {

    String INSERT_SQL = """
            INSERT INTO ROLE 
            	(TYPE) 
            VALUES 
            	(#{role.type, typeHandler=RoleTypeHandler})
            """;

    @Insert(value = INSERT_SQL)
    @Options(useGeneratedKeys = true, keyProperty = "role.id")
    @SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "role.id", before = false, resultType = long.class)
    int save(@Param("role") Role role);
}
