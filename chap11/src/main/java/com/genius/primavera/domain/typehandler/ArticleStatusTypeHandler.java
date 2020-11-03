package com.genius.primavera.domain.model.typehandler;

import com.genius.primavera.domain.model.article.ArticleStatus;
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
@MappedTypes(ArticleStatus.class)
public class ArticleStatusTypeHandler <E extends Enum<E>> extends BaseTypeHandler<ArticleStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ArticleStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getValue());
    }

    @Override
    public ArticleStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return ArticleStatus.of(rs.getInt(columnName));
    }

    @Override
    public ArticleStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return ArticleStatus.of(rs.getInt(columnIndex));
    }

    @Override
    public ArticleStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return ArticleStatus.of(cs.getInt(columnIndex));
    }
}
