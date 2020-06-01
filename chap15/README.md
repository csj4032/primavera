## chap15

### POI Excel database to file

### POI Excel file to database

### AutoConfiguration

### Sentry
* https://sentry.io 가입 혹은 로그인
* Settings > Project > Client Keys DNS 를 환경변수에 추가
* Logback appender 추가

```
<included>
    <appender name="SENTRY" class="io.sentry.logback.SentryAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>WARN</level>
        </filter>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
</included>
```

### PostgreSQL

```
docker pull postgres
docker run --name postgres -d -p 5432:5432 -e POSTGRES_PASSWORD=primavera -e POSTGRES_USER=primavera postgres
docker exec -it postgres bash

root@598a501b7d8e:/# psql -U postgres
psql (11.5 (Debian 11.5-1.pgdg90+1))
Type "help" for help.

postgres=# CREATE DATABASE primavera;
CREATE DATABASE

postgres=# \q

```