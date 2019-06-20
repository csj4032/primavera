## chap06

### Thymeleaf and AdminLTE
* Thymeleaf 의존성, Layout 설정

```
thymeleaf:
  cache: false
  enabled: true
  prefix: classpath:/templates/
  suffix: .html
```

```
implementation('org.springframework.boot:spring-boot-starter-thymeleaf')
compile('nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:2.3.0')
```

* AdminLTE 추가
* 로그인, 로그아웃 기능 추가

### Log4Jdbc

* build.gradle
```
compile('org.bgee.log4jdbc-log4j2:log4jdbc-log4j2-jdbc4.1:1.16')
```

* application.yml
```
datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: net.sf.log4jdbc.sql.jdbcapi.DriverSpy
    url: jdbc:log4jdbc:mariadb://localhost:3306/primavera
    username: primavera
    password: primavera
    hikari:
      connection-test-query: SELECT 1 FROM DUAL
```

* log4jdbc.log4j2.properties
```
log4jdbc.spylogdelegator.name = net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator
log4jdbc.dump.sql.maxlinelength = 0
```
* logback.xml
```    
<logger name="jdbc.sqlonly" level="DEBUG"/>
<logger name="jdbc.resultsettable" level="DEBUG"/>
```

### ETC
* thymeleaf [참고](https://www.thymeleaf.org/)
* adminLTE [참고](https://adminlte.io)