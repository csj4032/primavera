package com.genius.primavera.domain.typehandler;

import com.genius.primavera.domain.model.user.RoleType;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

@MappedJdbcTypes(JdbcType.INTEGER)
@MappedTypes(RoleType.class)
public class RoleTypeHandler<E extends Enum<E>> extends BaseTypeHandler<RoleType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, RoleType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public RoleType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getRoleType(rs.getInt(columnName));
    }

    @Override
    public RoleType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getRoleType(rs.getInt(columnIndex));
    }

    @Override
    public RoleType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getRoleType(cs.getInt(columnIndex));
    }

    private RoleType getRoleType(int type) {
        return Arrays.stream(RoleType.values()).filter(e -> e.getValue() == type).findFirst().orElseThrow();
    }
}