package com.genius.primavera.domain.model.typehandler;

import com.genius.primavera.domain.model.user.ProviderType;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

@MappedJdbcTypes(JdbcType.INTEGER)
@MappedTypes(ProviderType.class)
public class ProviderTypeHandler<E extends Enum<E>> extends BaseTypeHandler<ProviderType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ProviderType parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public ProviderType getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getRoleType(rs.getInt(columnName));
    }

    @Override
    public ProviderType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getRoleType(rs.getInt(columnIndex));
    }

    @Override
    public ProviderType getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getRoleType(cs.getInt(columnIndex));
    }

    private ProviderType getRoleType(int type) {
        return Stream.of(ProviderType.values()).filter(e -> e.getValue() == type).findFirst().orElseThrow();
    }
}
