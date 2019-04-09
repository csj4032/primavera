package com.genius.primavera.domain.mapper.support;

import com.genius.primavera.domain.model.Contact;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.sql.JDBCType;
import java.time.LocalDateTime;

public final class UserTableSupport {
	public static final UserTable userTable = new UserTable();
	public static final SqlColumn<Long> id = userTable.id;
	public static final SqlColumn<String> name = userTable.name;
	public static final SqlColumn<LocalDateTime> regDate = userTable.regDate;
	public static final SqlColumn<LocalDateTime> modDate = userTable.modDate;

	public static final class UserTable extends SqlTable {
		public final SqlColumn<Long> id = column("ID", JDBCType.INTEGER);
		public final SqlColumn<String> name = column("NAME", JDBCType.VARCHAR);
		public final SqlColumn<LocalDateTime> regDate = column("REG_DATE", JDBCType.DATE);
		public final SqlColumn<LocalDateTime> modDate = column("MOD_DATE", JDBCType.DATE);

		public UserTable() {
			super("USER");
		}
	}
}
