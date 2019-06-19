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

### ETC
* thymeleaf [참고](https://www.thymeleaf.org/)
* adminLTE [참고](https://adminlte.io)