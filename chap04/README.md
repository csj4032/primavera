## chap03
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