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

#### Spring StereoType
| 스테레오타입 | 설명 |
| @Component | 스프링에서 스프링 관리 컴포넌트로 인식하는 마커 |
| @Repository | 데이터 접근 객체의 역할을 수행 (Component 특화) |
| @Service | 서비스 계층의 역할 (Component 특화) |
| @Controller | 일반적으로 웹 컨텍스트에서 사용 (Component 특화) |

#### Constructor-based or setter-based DI?
* 단일 책임의 원칙 : 생성자의 인자가 많을 경우 코드량도 많아지고, 의존관계도 많아져 단일 책임의 원칙에 위배된다. 그래서 Constructor Injection을 사용함으로써 의존관계, 복잡성을 쉽게 알수 있어 리팩토링의 단초를 제공하게 된다.
* 테스트 용이성 : DI 컨테이너에서 관리되는 클래스는 특정 DI 컨테이너에 의존하지 않고 POJO여야 한다. DI 컨테이너를 사용하지 않고도 인스턴스화 할 수 있고, 단위 테스트도 가능하며, 다른 DI 프레임 워크로 전환할 수도 있게 된다.
* Immutability : Constructor Injection에서는 필드는 final로 선언할 수 있다. 불변 객체가 가능한데 비해 Field Injection은 final는 선언할 수 없기 때문에 객체가 변경 가능한 상태가 된다.
* 순환 의존성 : Constructor Injection에서는 멤버 객체가 순환 의존성을 가질 경우 BeanCurrentlyInCreationException이 발생해서 순환 의존성을 알 수 있게 된다.
* 의존성 명시 : 의존 객체 중 필수는 Constructor Injection을 옵션인 경우는 Setter Injection을 활용할 수 있다.

```
Description:

The dependencies of some of the beans in the application context form a cycle:

┌─────┐
|  injectionService defined in file [/Users/we/Workspace/primavera/chap01/out/production/classes/com/genius/primavera/application/InjectionService.class]
↑     ↓
|  injectionServiceCycle defined in file [/Users/we/Workspace/primavera/chap01/out/production/classes/com/genius/primavera/application/InjectionServiceCycle.class]
└─────┘

```

#### Bean Scope
| 스코프 | 설명 |
| singleton | 스프링 컨테이너가 단일 인스턴스를 리턴, 기본값 |
| prototype | 스프링 컨테이너가 요청을 받을 때마다 새로운 인스턴스를 생성 |
| request | 스프링 컨테이너가 각각의 HTTP 요청에 대응하여 새로운 인스턴스를 리턴, 웹 컨텍스트에서 사용 |
| session | 스프링 컨테이너가 Http 세션에 대응하여 새로운 인스턴스를 리턴, 웹 컨텍스트에서 사용 |
| application | ServletContext 의 수명 주기로 지정, 웹 컨텍스트에서 사용 |
| websocket | WebSocket 의 수명 주기로 지정, 웹 컨텍스트에서 사용 |

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
* Dependencies [링크](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#beans-dependencies)
  * https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#beans-constructor-injection
  * https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#beans-setter-injection (Constructor-based or setter-based DI?)
* Bean Scopes [링크](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#beans-factory-scopes)
* Using a Custom Scope [링크](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#beans-factory-scopes-custom-using)
* Creating Your Own Auto-configuration [링크](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-developing-auto-configuration.html#boot-features-developing-auto-configuration)
* Spring Expression Language The Elvis Operator [링크](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html#expressions-operator-elvis)
* Creating a Custom Starter with Spring Boot [링크](https://www.baeldung.com/spring-boot-custom-starter)
