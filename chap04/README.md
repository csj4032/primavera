## chap04

### Logback File Configuration

### Mybatis Auto Configuration
```
mybatis:
  configuration:
    map-underscore-to-camel-case: true
    default-fetch-size: 1000
    default-statement-timeout: 30
  type-aliases-package: com.genius.primavera.domain
  type-handlers-package: com.genius.primavera.domain
```

### WINNER TABLE
```
CREATE TABLE IF NOT EXISTS WINNER (
    ID int(11) NOT NULL AUTO_INCREMENT,
    USER_ID int(45) NOT NULL,
    WINNER enum('WINNER','LOSER') NOT NULL,
    REG_DT datetime NOT NULL,
    PRIMARY KEY (ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### Spring Boot Test
* WinnerServiceIsolationTest
* WinnerServicePropagationTest
* RoleMapperTest
* UserMapperTest (Dynamic Sql)
* WinnerMapperTest

### ACID (원자성, 일관성, 격리성, 지속성)
* 원자성(Atomicity) : 트랜잭션은 연속적인 액션들로 이루어진 원자성 작업, 트랜잭션의 액션은 전부다 수행되거나 아무것도 수행되지 안도록 보장 
* 일관성(Consistency) : 트랜잭션의 액션이 모두 완료되면 커밋되고 데이터 및 리소스는 비즈니스 규칙에 맞게 일관된 상태를 유지
* 격리성(Isolation) : 동일한 데이터 여러 트랜잭션이 동시에 처리할 경우 데이터가 변질되지 않게 하려면 각각의 트랜잭션을 격리
* 지속성(Durability) : 트랜잭션 완료 후 그 결과는 설령 시스템이 실패 하더라도 살마남아야 함 (보통 트랜잭션 결과물은 퍼시스턴스 저장소에 씌어짐)

### Spring Propagation
| 전달 속성 | 설명 |
|---|---|
| REQUIRED | 진행 중인 트랜잭션이 있으면 현재 메서드를 그 트랜잭션에서 실행하되, 그렇지 않을 경우 새 트랜잭션을 시작해서 실행 |
| REQUIRES_NEW | 항상 새 트랜잭션을 시작해 현재 메서드를 실행하고 진행 중인 트랜잭션이 있으면 잠시 중단 |
| SUPPORTS | 진행 중인 트랜잭션이 있으면 현재 메서드를 그 트랜잭션 내에서 실행하되, 그렇지 않을 경우 트랜잭션 없이 실행 |
| NOT_SUPPORTED | 트랜잭션 없이 현재 메서드를 실행하고 진행 중인 트랜잭션이 있으면 잠시 중단 시킴 |
| MANDATORY | 반드시 트랜잭션을 걸고 현재 메서드를 실행하되 진행 중인 트랜잭션이 없으면 예외를 던짐 |
| NEVER | 반드시 트랜잭션 없이 현재 메서드를 실행하되 진행 중인 트랜잭션이 있으면 예외를 던짐 |
| NESTED | 진행 중인 트랜잭션이 있으면 현재 메서드를 이 트랜잭션의 중첩 트랜잭션 내에서 실행함. 진행 중인 트랜잭션에 영향을 받음. 진행 중인 트랜잭션이 없으면 새 트랜잭션을 시작해서 실행함 이 기능은 스프링에서만 사용됨 |

### Read phenomena
* Dirty read : T2가 수정 후 커밋하지 않은 필드을 T1이 읽는 상황에서 나중에 T2가 롤백하면 T1이 읽은 필드는 일시적인 값으로 더 이상 유효하지 않음
* Nonrepeatable read : 어떤 필드를 T1이 읽은 후 T2가 수정할 경우, T1이 같은 필드를 다시 읽으면 다른 값을 얻음 
* Phantom read : T1이 테이블의 로우 몇 개를 읽은 후 T2가 같은 테이블에 새 로우를 삽입할 경우, 나중에 T1이 같은 테이블을 다시 읽으면 T2가 삽입한 로우가 보임
* Lost updates : T1, T2 모두 어떤 로우를 수정하려고 읽고 그 로우의 상태에 따라 수정하려는 경우. T1이 먼저 로우를 수정 후 커밋하기 전 T2가 T1이 수정한 로우를 똑같이 수정했다면 T1이 커밋한 후에 T2 역시 커밋을 하게 되면 T1이 수정한 로우를 T2가 덮어쓰게 되어 T1이 수정 한 내용이 소실

### Spring Isolation

| 격리 수준 | 설명 |
|---|---|
| DEFAULT | 데이터베이스 기본 격리 수준을 사용. 대다수 데이터베이스는 READ_COMMITTED 기본 격리 수준 |
| READ_UNCOMMITTED | 다른 트랜잭션이 아직 커밋하지 않은 값을 트랜잭션이 읽을 수 있음. 오염된 값 읽기, 재현 불가한 읽기, 허상 읽기 문제가 발생 |
| READ_COMMITTED | 한 트랜잭션이 다른 트랜잭션이 커밋한 값만 읽을 수 있음. 오염된 값 읽기 문제는 해결. 재현 불가한 일기, 허상 읽기 문제는 남음 |
| REPEATABLE_READ | 트랜잭션이 어떤 필드를 여러 번 읽어도 동일한 값을 읽도록 보장. 트랜잭션이 지속되는 동안에는 다른 트랜잭션이 해당 필드를 변경 할 수 없음. 오염된 값 읽기, 재현 불가한 읽기 문제는 해결되지만 허상 읽기는 여젼히 숙제 |
| SERIALIZABLE | 트랜잭션이 테이블을 여러 번 읽어도 정확히 동일한 로우를 읽도록 보장. 트랜잭션이 지속되는 동안에는 다른 트랜잭션이 해당 테이블에 삽입 수정, 삭제를 할 수 없음. 동시성 문제는 모두 해소되지만 성능은 현저히 떨어짐 |

### ETC
* logback [참고](https://logback.qos.ch/)
* mybatis [참고](http://www.mybatis.org/mybatis-3/)
* mybatis Dynamic SQL [참고](http://www.mybatis.org/mybatis-dynamic-sql/docs/introduction.html)
* spring-boot-autoconfigure [참고](http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
