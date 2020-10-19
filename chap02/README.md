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
| AOP | 설명 |
| Aspect | 포인트컷과 관련 어드바이스의 집합 |
| Advice | 특정 포인트컷에 있는 조인 포인트에서 실행될 액션  |
| Pointcut | 어드바이스를 하나 또는 여러 조인트 포임트와 결합하는 패턴 |
| Join Point | 어드바이스가 실행될 메서드 예외 지점  |

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
* Testing [참고](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html)
* Mariadb-connector-j [참고](https://mariadb.com/kb/en/library/about-mariadb-connector-j/)
* Mariadb-java-client [링크](https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client)