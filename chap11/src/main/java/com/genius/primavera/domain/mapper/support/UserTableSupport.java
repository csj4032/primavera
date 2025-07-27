package com.genius.primavera.domain.mapper.support;

import com.genius.primavera.domain.model.user.UserStatus;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.sql.JDBCType;
import java.time.LocalDateTime;

public final class UserTableSupport {
	public static final UserTable userTable = new UserTable();
	public static final SqlColumn<Long> id = userTable.id;
	public static final SqlColumn<String> email = userTable.email;
	public static final SqlColumn<String> password = userTable.password;
	public static final SqlColumn<String> nickname = userTable.nickname;
	public static final SqlColumn<UserStatus> status = userTable.status;
	public static final SqlColumn<LocalDateTime> createdAt = userTable.createdAt;
	public static final SqlColumn<LocalDateTime> updatedAt = userTable.updatedAt;

	public static final class UserTable extends SqlTable {
		public final SqlColumn<Long> id = column("ID", JDBCType.INTEGER);
		public final SqlColumn<String> email = column("EMAIL", JDBCType.VARCHAR);
		public final SqlColumn<String> password = column("PASSWORD", JDBCType.VARCHAR);
		public final SqlColumn<String> nickname = column("NICKNAME", JDBCType.VARCHAR);
		public final SqlColumn<UserStatus> status = column("STATUS", JDBCType.VARCHAR, "UserStatusTypeHandler");
		public final SqlColumn<LocalDateTime> createdAt = column("CREATED_AT", JDBCType.DATE);
		public final SqlColumn<LocalDateTime> updatedAt = column("UPDATED_AT", JDBCType.DATE);

		public UserTable() {
			super("USER");
		}
	}
}
