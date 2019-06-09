## chap01
### 스프링 부트 설정
#### 프로젝트 기본 설정
* Spring Initializr [링크](https://start.spring.io/) 
* Application Properties [참고](https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html)
* Spring boot banner [링크](https://devops.datenkollektiv.de/banner.txt/index.html)

#### Dependency Injection

#### 테스트 설정
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

### 테스트
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

* HelloController
    * MockMvc 테스트
    * MockBean 테스트
