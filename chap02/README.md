## chap02

### Spring Boot Test
* HelloControllerTest
  * @WebMvcTest, @MockBean 이용
* PrimaveraApplicationTest
* HelloJsonTest

### MariaDB 연결 및 테스트
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

desribe article; 
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
* Testing [참고](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html)
* Mariadb-connector-j [참고](https://mariadb.com/kb/en/library/about-mariadb-connector-j/)
* Mariadb-java-client [링크](https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client)