## chap03

### Application.yml
```
  datasource:
    driver-class-name: org.mariadb.jdbc.Driver
    url: jdbc:mariadb://localhost:3306/primavera
    username: primavera
    password: primavera
```

### Spring Boot Test
* HikariDataSourceTest
```
com.zaxxer.hikari.HikariConfig - Driver class org.mariadb.jdbc.Driver found in Thread context class loader jdk.internal.loader.ClassLoaders$AppClassLoader@3d4eac69
com.zaxxer.hikari.HikariConfig - HikariPool-1 - configuration:
com.zaxxer.hikari.HikariConfig - allowPoolSuspension.............false
com.zaxxer.hikari.HikariConfig - autoCommit......................true
com.zaxxer.hikari.HikariConfig - catalog.........................none
com.zaxxer.hikari.HikariConfig - connectionInitSql..............."SELECT 1 FROM DUAL"
com.zaxxer.hikari.HikariConfig - connectionTestQuery.............none
com.zaxxer.hikari.HikariConfig - connectionTimeout...............30000
com.zaxxer.hikari.HikariConfig - dataSource......................none
com.zaxxer.hikari.HikariConfig - dataSourceClassName.............none
com.zaxxer.hikari.HikariConfig - dataSourceJNDI..................none
com.zaxxer.hikari.HikariConfig - dataSourceProperties............{password=<masked>}
com.zaxxer.hikari.HikariConfig - driverClassName................."org.mariadb.jdbc.Driver"
com.zaxxer.hikari.HikariConfig - healthCheckProperties...........{}
com.zaxxer.hikari.HikariConfig - healthCheckRegistry.............none
com.zaxxer.hikari.HikariConfig - idleTimeout.....................600000
com.zaxxer.hikari.HikariConfig - initializationFailTimeout.......1
com.zaxxer.hikari.HikariConfig - isolateInternalQueries..........false
com.zaxxer.hikari.HikariConfig - jdbcUrl.........................jdbc:mariadb://localhost:3306/primavera
com.zaxxer.hikari.HikariConfig - leakDetectionThreshold..........0
com.zaxxer.hikari.HikariConfig - maxLifetime.....................1800000
com.zaxxer.hikari.HikariConfig - maximumPoolSize.................10
com.zaxxer.hikari.HikariConfig - metricRegistry..................none
com.zaxxer.hikari.HikariConfig - metricsTrackerFactory...........none
com.zaxxer.hikari.HikariConfig - minimumIdle.....................10
com.zaxxer.hikari.HikariConfig - password........................<masked>
com.zaxxer.hikari.HikariConfig - poolName........................"HikariPool-1"
com.zaxxer.hikari.HikariConfig - readOnly........................false
com.zaxxer.hikari.HikariConfig - registerMbeans..................false
com.zaxxer.hikari.HikariConfig - scheduledExecutor...............none
com.zaxxer.hikari.HikariConfig - schema..........................none
com.zaxxer.hikari.HikariConfig - threadFactory...................internal
com.zaxxer.hikari.HikariConfig - transactionIsolation............default
com.zaxxer.hikari.HikariConfig - username........................"primavera"
com.zaxxer.hikari.HikariConfig - validationTimeout...............5000
com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Starting...
com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Added connection org.mariadb.jdbc.MariaDbConnection@3ba9ad43
com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Start completed.
```

* SpringDataSourceTest
```
Caused by: org.springframework.beans.BeanInstantiationException: 
Failed to instantiate [com.zaxxer.hikari.HikariDataSource]: Factory method 'dataSource' threw exception; 
nested exception is org.springframework.boot.autoconfigure.jdbc.DataSourceProperties$DataSourceBeanCreationException: Failed to determine a suitable driver class
```

* UserDaoTest
  * org.springframework.boot.autoconfigure.jdbc.DataSourceConfiguration 확인
  * Datasource 를 통한 유저 등록, 전체 조회, 삭제 테스트
  * PasswordEncoderFactories 이용한 비밀번호 암호

### ETC
* application-properties datasource [참고](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)
* HikariCP [참고](https://github.com/brettwooldridge/HikariCP)
* tomcat datasource [참고](https://tomcat.apache.org/tomcat-9.0-doc/jdbc-pool.html)
* dbcp2 datasource [참고](https://commons.apache.org/proper/commons-dbcp/)