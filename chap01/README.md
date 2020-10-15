## chap01

### 스프링 부트 환경 설정

#### JDK 설치
* JDK Download  [링크](https://www.oracle.com/technetwork/java/javase/downloads/index.html)

#### IntelliJ 설치
* IDEA Download [링크](https://www.jetbrains.com/idea/download)

#### 프로젝트 생성
* Spring CLI [링크](https://docs.spring.io/spring-boot/docs/current/reference/html/getting-started-installing-spring-boot.html#getting-started-installing-the-cli)
* Spring Initializr [링크](https://start.spring.io/)
* Spring Boot Reference Guide [링크](https://docs.spring.io/spring-boot/docs/current/reference/html/)
* Spring Boot Application Properties [참고](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)
* Building Spring Boot 2 Applications with Gradle [링크](https://guides.gradle.org/building-spring-boot-2-projects-with-gradle/)

##### Spring CLI
```
$ spring init --list
$ spring init --build=gradle --java-version=1.8 --dependencies=web,thymeleaf --groupId=com.genius.primavera primavera
```

##### Gradle Project
```
$ mkdir ~/gradle-spring-boot-project
$ cd ~/gradle-spring-boot-project
$ gradle init  --type java-application
```

##### Gradle Version
```
$ ./gradle wrapper --gradle-version 5.3.1
$ ./gradle -v
```

###### build.gradle
```
plugins {
    id 'java'
    id 'com.gradle.build-scan' version '2.0.2'
    id 'org.springframework.boot' version '2.0.5.RELEASE'
    id 'io.spring.dependency-management' version '1.0.7.RELEASE'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-dependencies:2.0.5.RELEASE'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    components {
        withModule('org.springframework:spring-beans') {
            allVariants {
                withDependencyConstraints {
                    // Need to patch constraints because snakeyaml is an optional dependency
                    it.findAll { it.name == 'snakeyaml' }.each { it.version { strictly '1.19' } }
                }
            }
        }
    }
}
```

###### dependencies
```
$ gradle dependencies
```

#### HelloWorld
```java
package com.genius.primavera.interfaces;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/")
public class HelloController {

	@GetMapping
	public String helloWorld() {
		return "Hello World";
	}
}
```

#### Application.yml 설정
```
spring:
  profiles: default
  application:
    name: Privamera
  banner:
    charset: UTF-8
    location: classpath:primavera.txt

logging:
  level:
    org.springframework: debug
    com.genius.primavera: debug

com:
  genius:
    primavera:
      username: primavera
      password: primavera
      url: jdbc:mariadb://localhost:3306/primavera
      tables: user, role
      params:
        keyword: genius
        page: 1
        sort: desc
      users:
        - id: 1
          email: genius
        - id: 2
          email: genius2

---

spring:
  profiles: dev
  application:
    name: Privamera
  banner:
    charset: UTF-8
    location: classpath:primavera.txt
logging:
  level:
    org.springframework: warn
    com.genius.primavera: warn
```
* application-{profile}.yml
* properties 는 동일한 계층을 반복적으로 작성
* yml 파일은 각 설정이 계층적으로 구성

#### Gradle Build And Start
```
$ chmod 755 gradlew

$ ./gradlew build && java -jar chap01/build/libs/chap01-0.0.1-SNAPSHOT.jar -D spring.profiles.active=dev

$ gradle -b chap01/build.gradle bootRun -DmainClass=com.genius.primavera.PrimaveraApplication

$ gradle :chap01:bootRun -DmainClass=com.genius.primavera.PrimaveraApplication
```

#### ConfigurationTest : Bean, YAML

#### SpringBootApplication
* @SpringBootConfiguration
  * 스프링 부트의 설정을 나타내는 어노테이션
  * 스프링 @Configuration 을 (1.4.0 이후) 대체하며 스프링 부트 전용
* @EnableAutoConfiguration
  * 자동 설정의 핵심 어노테이션
  * 클래스 경로에 지정된 내용을 기반으로 영리하게 설정 자동화를 수행
* @ComponentScan
  * 특정 패키지 경로를 기반으로 @Configuration에서 사용할 @Component 설정 클래스를 찾음

#### @EnableAutoConfiguration
* AutoConfigurationImportSelector
* Locating Auto-configuration Candidates (META-INF/spring.factories)
* Condition Annotations

#### @EnableAspectJAutoProxy
```
spring:
  aop:
    proxy-target-class: true
```
* application.yml 설정과 어노테이션 관계
* https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#aop-introduction-proxies
* https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#aop-enable-aspectj-java

#### Test Configuration (Spring Boot 2.2 Release 버전 이후 Junit 5 기본)
* Junit 5 적용을 위한 gradle.build 설정
* Junit 5 [참고](https://junit.org/junit5/docs/current/user-guide/)

```
testImplementation('org.springframework.boot:spring-boot-starter-test') {
        exclude module: 'junit'
}

testImplementation group: 'org.junit.jupiter', name: 'junit-jupiter-api', version: '5.3.2'
testCompile group: 'org.junit.jupiter', name: 'junit-jupiter-params', version: '5.3.2'
testRuntime group: 'org.junit.jupiter', name: 'junit-jupiter-engine', version: '5.3.2'
```

### Retrofit2
* https://square.github.io/retrofit/

### ETC
* Spring boot test [참고](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html)
* Spring boot banner [링크](https://devops.datenkollektiv.de/banner.txt/index.html)
* Creating Your Own Auto-configuration [링크](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-developing-auto-configuration.html#boot-features-developing-auto-configuration)
* Creating a Custom Starter with Spring Boot [링크](https://www.baeldung.com/spring-boot-custom-starter)
