## chap04
### Mybatis
### Mybatis Auto Configuration 추가
* [참고](http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
* PrimaveraApplicationTest 실행 에러 로그 확인

```
dependencies {
  compile("org.mybatis.spring.boot:mybatis-spring-boot-starter:2.0.1")
}
```

* Application.yml 설정 추가

```
# application.yml
mybatis:
    type-aliases-package: com.example.domain.model
    type-handlers-package: com.example.typehandler
    configuration:
        map-underscore-to-camel-case: true
        default-fetch-size: 100
        default-statement-timeout: 30
...
```

* User 등록, 수정, 아이디 검색, 전체 검색, 주소록 포함 검색
* mybatis-dynamic-sql 이용한 User 조회

```
org.springframework.dao.DataIntegrityViolationException: 
### Error updating database.  Cause: java.sql.SQLDataException: (conn=271) Data too long for column 'STATUS' at row 1
### The error may exist in com/genius/primavera/domain/mapper/UserMapper.java (best guess)
### The error may involve com.genius.primavera.domain.mapper.UserMapper.save-Inline
### The error occurred while setting parameters
### SQL: INSERT INTO USER (EMAIL, PASSWORD, NICK_NAME, STATUS, REG_DATE, MOD_DATE) VALUES (?, ?,?, ?, ?, ?)
### Cause: java.sql.SQLDataException: (conn=271) Data too long for column 'STATUS' at row 1
; (conn=271) Data too long for column 'STATUS' at row 1; nested exception is java.sql.SQLDataException: (conn=271) Data too long for column 'STATUS' at row 1
```
* STATUS 컬럼 typeHandler 적용
    * type-handler 등록은 type-alias 도 함께 등록