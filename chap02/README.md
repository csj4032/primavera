## chap02

### Spring MVC
| MVC | 역할 |
|---|---|
| Model | 애플리케이션의 정보, 즉 데이터를 의미 |
| View | 사용자 인터페이스, 즉 사용자가 보고 사용하는 화면 등을 의미 |
| Controller | 모델과 뷰의 중계 역할, 사용자의 요청을 파악하고 그에 맞는 결과를 사용자에게 돌려줌 |

### Spring WEB MVC
![The request processing workflow in Spring Web MVC (high level)](https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/images/mvc.png)
![Typical context hierarchy in Spring Web MVC](https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/images/mvc-context-hierarchy.png)
![Single root context in Spring Web MVC](https://docs.spring.io/spring/docs/4.3.9.RELEASE/spring-framework-reference/html/images/mvc-root-context.png)

### Spring AOP

* Aspect
  * 어드바이스와 포인트컷을 합친 것
  * 두 가지 정보가 합쳐지면 애스펙트가 무엇을 언제 어떻게 할지, 즉 애스펙트에 필요한 모든 정보가 정의
  * @Aspect 어노테이션으로 어노테이션이있는 경우 클래스 선언은 Spring에서 aspect로 인식
* Weaving
  * 타깃 객체에 애스펙트를 적용해서 새로운 프록시 객체를 생성하는 절차
    * 컴파일시간
    * 클래스로드시간
    * 실행시간
* Join point
  * 어드바이스를 적용할 수 있는 곳을 조인포인트
  * 애플리케이션 실행에 애스펙트를 끼워 넣을 수 있는 지점
* Target object
  * 부가기능을 부여할 대상
* Advice
  * Before advice
    * 어드바이스 대상 메서드가 호출되기 전에 어드바이스 기능을 수행
  * After returning advice
    * 어드바이스 대상 메서드가 성공적으로 완료된 후에 어드바이스 기능을 수행
  * After throwing advice
    * 어드바이스 대상 메서드가 예외를 던진 후에 어드바이스 기능을 수행
  * After (finally) advice
    * 결과에 상관없이 어드바이스 대상 메서드가 완료된 후에 어드바이스 기능을 수행
  * Around advice
    * 어드바이스가 어드바이스 대상 메서드를 감싸서 어드바이스 대상 메서드 호출 전과 후에 몇 가지 기능을 제공
* Pointcut
  * 애스펙트가 어드바이스할 조인포인트의 영역을 좁히는 일을 함
  * 어드바이스는 애스펙트가 무엇을 언제 할지를 정의한다면, 포인트컷은 어디서 할지를 정의
  * 각 포인트컷은 어드바이스가 위빙돼야 하는 하나 이상의 조인포인트를 정의
* Introduction:
  * 기존 클래스에 코드 변경 없이도 새 메소드나 멤버 변수를 추가하는 기능

| AOP | 설명 |
|---|---|
| Aspect | 포인트컷과 관련 어드바이스의 집합 |
| Advice | 특정 포인트컷에 있는 조인 포인트에서 실행될 액션  |
| Pointcut | 어드바이스를 하나 또는 여러 조인트 포임트와 결합하는 패턴 |
| Join Point | 어드바이스가 실행될 메서드 예외 지점  |

### Spring Interceptor


### Spring Boot Test
* HelloControllerTest
  * @WebMvcTest, @MockBean 이용
* PrimaveraApplicationTest
* HelloJsonTest

### MariaDB Connection Test
* DatabaseConnectionComponentTest
* DatabaseConnectionTest

### Docker For MariaDB

#### Downloading an Image

```
docker search mariadb
docker pull mariadb:latest
```

#### Creating a Container

```
docker run --name mariadb -d -p 3306:3306 -e MYSQL_ROOT_HOST=% -e MYSQL_ROOT_PASSWORD=root -e MYSQL_USER=primavera -e MYSQL_PASSWORD=primavera -e MYSQL_DATABASE=primavera mariadb:latest

docker ps

docker exec -it mariadb bash

mysql -u primavera -h 127.0.0.1 -p primavera

show databases;

use primavera;

show tables;

```

#### Running and Stopping the Container

```
docker restart mariadb
docker stop mariadb
docker start mariadb

docker rm maraidb
docker rm -v maraidb
```

### ETC
* AOP Concepts [참고](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#aop-introduction-defn)
* Aspect Oriented Programming with Spring [참고](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#aop)
* Interception [참고](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/web.html#mvc-handlermapping-interceptor)
* Testing [참고](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html)
* Mariadb-connector-j [참고](https://mariadb.com/kb/en/library/about-mariadb-connector-j/)
* Mariadb-java-client [링크](https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client)