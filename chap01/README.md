## chap01
### Spring Boot Start
#### Project Initialization
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

#### Test Configuration
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

##### 참고
* 스프링부트 테스트 [참고](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html)
* PrimaveraApplicationTest

```java
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class PrimaveraApplicationTest {

	@Test
	@DisplayName(value = "스프링부트가 정상 작동하는지 알아보자.")
	public void helloWorld() {

	}
}
```

### Application.yml 언제 어떻게 읽을까?
1. PrimaveraApplication
2. SpringApplicationBuilder 에서 SpringApplication 생성 후 run
3. SpringApplication 생성 중 ApplicationListener 등록 (META-INF/spring.factories 파일 참조)
4. ConfigFileApplicationListener 통하여 application.yml 로딩

### Database 어떻게 연결되나?
1. DataSourceAutoConfiguration matched
2. DataSourceConfiguration.PooledDataSourceConfiguration matched
3. DataSourceConfiguration.Hikari matched

#### SpringBootApplication
* 스프링 컴포넌트 검색과 스프링 부트 자동 구성을 활성화 

##### @Configuration
* 이 어노테이션이 붙은 클래스를 스프링의 자바 기반 구성 클래스로 저장

##### @ComponentScan
* 컴포넌트 검색 기능을 활성화해서 웹 컨트롤러 클래스나 다른 컴포넌트 클래스들을 자동으로 검색하여 스프링 애플리케이션 컨텍스트에 빈으로 등록시킴

##### @EnableAutoConfiguration
* 스프링 부트의 자동 구성

##### SpringBean Bean 생성 및 테스트

##### HelloController 이용한 SpringBean 접근

### ETC
* Spring boot banner [링크](https://devops.datenkollektiv.de/banner.txt/index.html)
