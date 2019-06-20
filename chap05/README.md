## chap05

### LiveReload
* build.gradle
```
dependencies {
    implementation('org.springframework.boot:spring-boot-devtools')
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
  * 저장과 수정에 따른 필수값 그룹
  * 객체 내부 속성 @Valid 재귀 검사
  
### Spring Boot Test
* AjaxControllerTest
* UserSaveValidationTest
* UserUpdateValidationTest

### ETC
* Spring MVC Test Framework [참고](https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#spring-mvc-test-framework)
* LiveReload [링크](http://livereload.com/extensions/)