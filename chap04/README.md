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

### ACID (원자성, 일관성, 격리성, 지속성)
* 원자성(Atomicity) : 트랜잭션은 연속적인 액션들로 이루어진 원자성 작업, 트랜잭션의 액션은 전부다 수행되거나 아무것도 수행되지 안도록 보장 
* 일관성(Consistency) : 트랜잭션의 액션이 모두 완료되면 커밋되고 데이터 및 리소스는 비즈니스 규칙에 맞게 일관된 상태를 유지
* 격리성(Isolation) : 동일한 데이터 여러 트랜잭션이 동시에 처리할 경우 데이터가 변질되지 않게 하려면 각각의 트랜잭션을 격리
* 지속성(Durability) : 트랜잭션 완료 후 그 결과는 설령 시스템이 실패 하더라도 살마남아야 함 (보통 트랜잭션 결과물은 퍼시스턴스 저장소에 씌어짐)

### Spring Propagation
| 전딜 속성 | 설명 |
|---|---|
| REQUIRED | 진행 중인 트랜잭션이 있으면 현재 메서드를 그 트랜잭션에서 실행하되, 그렇지 않을 경우 새 트랜잭션을 시작해서 실행 |
| REQUIRES_NEW | 항상 새 트랜잭션을 시작해 현재 메서드를 실행하고 진행 중인 트랜잭션이 있으면 잠시 중단 |
| SUPPORTS | 진행 중인 트랜잭션이 있으면 현재 메서드를 그 트랜잭션 내에서 실행하되, 그렇지 않을 경우 트랜잭션 없이 실행 |
| NOT_SUPPORTED | 트랜잭션 없이 현재 메서드를 실행하고 진행 중인 트랜잭션이 있으면 잠시 중단 시킴 |
| MANDATORY | 반드시 트랜잭션을 걸고 현재 메서드를 실행하되 진행 중인 트랜잭션이 없으면 예외를 던짐 |
| NEVER | 반드시 트랜잭션 없이 현재 메서드를 실행하되 진행 중인 트랜잭션이 있으면 예외를 던짐 |
| NESTED | 진행 중인 트랜잭션이 있으면 현재 메서드를 이 트랜잭션의 중첩 트랜잭션 내에서 실행함. 진행 중인 트랜잭션이 없으면 새 트랜잭션을 시작해서 실행함 |

### 트랜잭션 격리 속성
* Dirty read : T2가 수정 후 커밋하지 않은 필드을 T1이 읽는 상황에서 나중에 T2가 롤백하면 T1이 읽은 필드는 일시적인 값으로 더 이상 유효하지 않음
* Nonrepeatable read : 어떤 필드를 T1이 읽은 후 T2가 수정할 경우, T1이 같은 필드를 다시 읽으면 다른 값을 얻음 
* Phantom read : T1이 테이블의 로우 몇 개를 읽은 후 T2가 같은 테이블에 새 로우를 삽입할 경우, 나중에 T1이 같은 테이블을 다시 읽으면 T2가 삽입한 로우가 보임
* Lost updates : T1, T2 모두 어떤 로우를 수정하려고 읽고 그 로우의 상태에 따라 수정하려는 경우. T1이 먼저 로우를 수정 후 커밋하기 전 T2가 T1이 수정한 로우를 똑같이 수정했다면 T1이 커밋한 후에 T2 역시 커밋을 하게 되면 T1이 수정한 로우를 T2가 덮어쓰게 되어 T1이 수정 한 내용이 소실

### Spring Isolation

| 격리 수준 | 설명 |
|---|---|
| DEFAULT | DB 기본 격리 수준을 사용합니다. 대다수 DB는 READ_COMMITTED 기본 격리 수준|
| READ_UNCOMMITTED | 다른 트랜잭션이 아직 커밋하지 않은 값을 트랜잭션이 읽을 수 있음. 오염된 값 읽기, 재현 불가한 읽기, 허상 읽기 문제가 발생 |
| READ_COMMITTED | 한 트랜잭션이 다른 트랜잭션이 커밋한 값만 읽을 수 있음. 오염된 값 읽기 문제는 해결. 재현 불가한 일기, 허상 읽기 문제는 남음 |
| REPEATABLE_READ | 트랜잭션이 어떤 필드를 여러 번 읽어도 동일한 값을 읽도록 보장. 트랜잭션이 지속되는 동안에는 다른 트랜잭션이 해당 필드를 변경 할 수 없음. 오염된 값 읽기, 재현 불가한 읽기 문제는 해결되지만 허상 읽기는 여젼히 숙제 |
| SERIALIZABLE | 트랜잭션이 테이블을 여러 번 읽어도 정확히 동일한 로우를 읽도록 보장. 트랜잭션이 지속되는 동안에는 다른 트랜잭션이 해당 테이블에 삽입 수정, 삭제를 할 수 없음. 동시성 문제는 모두 해소되지만 성능은 현저히 떨어짐 |
