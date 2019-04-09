## chap02
### MariaDB 연결 및 테스트
### 연결
* Mariadb-connector-j [참고](https://mariadb.com/kb/en/library/about-mariadb-connector-j/)
* Mariadb-java-client [링크](https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client)

### 테스트
* DatabaseConnectionTest
    * catalog 확인 테스트

```
public static Connection getConnection() {
		try {
			DriverManager.registerDriver(new org.mariadb.jdbc.Driver());
			return DriverManager.getConnection(URL, "study", "study");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
```

* DatabaseConnectionComponentTest
    * Bean 생성 및 catalog 확인 테스트

```
@Component
public class DatabaseConnectionComponent {

	@Value(value = "${mariadb.url}")
	public String url;
	@Value(value = "${mariadb.user}")
	public String user;
	@Value(value = "${mariadb.password}")
	public String password;

	public Connection getConnection() {
	}
}
```
