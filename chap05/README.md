## chap05

### LiveReload
* build.gradle
```
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-validation')
    implementation('org.springframework.boot:spring-boot-devtools')
    implementation('org.graalvm.js:js:20.2.0')
    implementation('org.graalvm.js:js-scriptengine:20.2.0')
}
```
* application.yml
```
spring:
  devtools:
    livereload:
      enabled: true
      port: 35729
```
* livereload 설치

### Validation
* UserSaveValidationTest
  * @Nickname Validator, Annotation 
  * @ScriptAssert
  * 저장과 수정에 따른 필수값 그룹
  * 객체 내부 속성 @Valid 재귀 검사
  
### Spring Boot Test
* AjaxControllerTest
* UserSaveValidationTest
* UserUpdateValidationTest

### JPA Relation Basic
```sql
CREATE DATABASE basic DEFAULT CHARACTER SET utf8mb4;
CREATE USER 'relation'@'%' IDENTIFIED BY 'relation';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER ON basic.* TO 'relation'@'%';
```

### ETC
* Spring MVC Test Framework [참고](https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#spring-mvc-test-framework)
* Spring-boot-data-source-decorator [참고](https://github.com/gavlyukovskiy/spring-boot-data-source-decorator)
* Migration from Nashorn to GraalVM JavaScript [참고](https://golb.hplar.ch/2020/04/java-javascript-engine.html)
* Hibernate Validator[참고](https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/)
* LiveReload [링크](http://livereload.com/extensions/)