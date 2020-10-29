package com.genius.primavera.domain.mapper.support;

import com.genius.primavera.domain.model.UserStatus;
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
	public static final SqlColumn<LocalDateTime> regDate = userTable.regDate;
	public static final SqlColumn<LocalDateTime> modDate = userTable.modDate;

	public static final class UserTable extends SqlTable {
		public final SqlColumn<Long> id = column("ID", JDBCType.INTEGER);
		public final SqlColumn<String> email = column("EMAIL", JDBCType.VARCHAR);
		public final SqlColumn<String> password = column("PASSWORD", JDBCType.VARCHAR);
		public final SqlColumn<String> nickname = column("NICKNAME", JDBCType.VARCHAR);
		public final SqlColumn<UserStatus> status = column("STATUS", JDBCType.VARCHAR, "UserStatusTypeHandler");
		public final SqlColumn<LocalDateTime> regDate = column("REG_DT", JDBCType.DATE);
		public final SqlColumn<LocalDateTime> modDate = column("MOD_DT", JDBCType.DATE);

		public UserTable() {
			super("USER");
		}
	}
}
