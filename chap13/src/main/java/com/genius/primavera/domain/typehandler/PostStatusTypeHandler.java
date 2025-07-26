package com.genius.primavera.domain.model.typehandler;

import com.genius.primavera.domain.model.post.PostStatus;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedJdbcTypes(JdbcType.INTEGER)
@MappedTypes(PostStatus.class)
public class PostStatusTypeHandler <E extends Enum<E>> extends BaseTypeHandler<PostStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PostStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public PostStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return PostStatus.of(rs.getInt(columnName));
    }

    @Override
    public PostStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return PostStatus.of(rs.getInt(columnIndex));
    }

    @Override
    public PostStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return PostStatus.of(cs.getInt(columnIndex));
    }
}