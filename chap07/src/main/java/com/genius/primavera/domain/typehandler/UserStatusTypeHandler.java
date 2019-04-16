package com.genius.primavera.domain.typehandler;

import com.genius.primavera.domain.model.UserStatus;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

@MappedJdbcTypes(JdbcType.CHAR)
@MappedTypes(UserStatus.class)
public class UserStatusTypeHandler<E extends Enum<E>> extends BaseTypeHandler<UserStatus> {

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, UserStatus parameter, JdbcType jdbcType) throws SQLException {
		ps.setInt(i, parameter.getValue());
	}

	@Override
	public UserStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return getUserStatus(rs.getInt(columnName));
	}

	@Override
	public UserStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return getUserStatus(rs.getInt(columnIndex));
	}

	@Override
	public UserStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return getUserStatus(cs.getInt(columnIndex));
	}

	private UserStatus getUserStatus(int type) {
		return Arrays.stream(UserStatus.values()).filter(e -> e.getValue() == type).findFirst().orElseThrow();
	}
}
