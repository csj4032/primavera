# Chapter 01 - Spring Boot 시작하기 🌱

## 📋 개요
Spring Boot의 핵심 개념과 애플리케이션 구동 원리를 이해하는 기초 챕터입니다. `@SpringBootApplication` 어노테이션의 내부 동작과 자동 구성(Auto Configuration) 메커니즘을 심도 있게 학습합니다.

## 🎯 학습 목표
- **Spring Boot 핵심 어노테이션** 이해 및 활용
- **자동 구성(Auto Configuration)** 동작 원리 파악
- **SpringApplicationBuilder** 패턴 마스터
- **Domain-Driven Design** 기초 개념 적용

## 🛠️ 핵심 기술 스택
- **Java 21** - Record, Switch Expression 활용
- **Spring Boot 3.5.3** - 최신 부트 프레임워크
- **Spring Context** - IoC 컨테이너 관리
- **Gradle 8.12.1** - 빌드 자동화

## 📚 주요 학습 내용

### 1. @SpringBootConfiguration 심화 분석

`@SpringBootConfiguration`은 Spring Boot 애플리케이션의 설정을 나타내는 핵심 어노테이션입니다.

**주요 특징:**
- **내부적으로 @Configuration 포함**: 빈 정의의 소스임을 명시
- **단일 @SpringBootConfiguration**: 애플리케이션당 하나만 존재
- **컴포넌트 스캔의 시작점**: 패키지 기반 자동 빈 등록
- **테스트 환경 구성**: @SpringBootTest와 연동
- **@SpringBootApplication과의 관계**: 3가지 어노테이션 통합

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@Indexed
public @interface SpringBootConfiguration {
    @AliasFor(annotation = Configuration.class)
    boolean proxyBeanMethods() default true;
}
```

### 2. @EnableAutoConfiguration 메커니즘

Spring Boot의 자동 구성 기능을 활성화하는 핵심 어노테이션입니다.

**핵심 동작 원리:**
- **META-INF/spring.factories** 파일 기반 구성 클래스 로드
- **조건부 구성**: @ConditionalOnClass, @ConditionalOnBean 등
- **우선순위 관리**: @AutoConfigureOrder, @AutoConfigureBefore
- **제외 설정**: exclude 속성으로 특정 구성 제외

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
    String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";
    Class<?>[] exclude() default {};
    String[] excludeName() default {};
}
```

### 3. Auto Configuration 동작 플로우

```mermaid
flowchart TB
    start([EnableAutoConfiguration]) --> importSelector[ImportSelector]
    importSelector --> autoConfigImport[AutoConfigurationImportSelector]
    autoConfigImport --> getAutoConfigurationEntry[getAutoConfigurationEntry]
    
    getAutoConfigurationEntry --> getCandidateConfigurations[getCandidateConfigurations]
    getCandidateConfigurations --> loadFactoryNames[loadFactoryNames]
    loadFactoryNames --> springFactories["META-INF/spring.factories 로드"]
    
    getAutoConfigurationEntry --> removeDuplicates[removeDuplicates]
    removeDuplicates --> exclude[getExclusions]
    exclude --> filter[filter]
    filter --> fireEvents[fireAutoConfigurationImportEvents]
    
    fireEvents --> processImports[processImports]
    processImports --> conditionalOnClass["@ConditionalOnClass"]
    processImports --> conditionalOnBean["@ConditionalOnBean"]
    processImports --> conditionalOnProperty["@ConditionalOnProperty"]
    
    conditionalOnClass --> autoConfigure["자동 구성 활성화"]
    conditionalOnBean --> autoConfigure
    conditionalOnProperty --> autoConfigure
    
    autoConfigure --> beanRegistration["빈 등록 완료"]
```

### 4. SpringApplicationBuilder 패턴

유연한 애플리케이션 구성을 위한 빌더 패턴 기반 클래스입니다.

**주요 기능:**
- **유연한 애플리케이션 구성**: 메서드 체이닝 지원
- **계층적 구성**: parent-child 관계 설정
- **프로파일 및 속성 설정**: 환경별 구성 관리
- **웹 애플리케이션 타입 지정**: SERVLET, REACTIVE, NONE
- **리스너 및 이니셜라이저 추가**: 이벤트 기반 확장

```java
public class PrimaveraApplication {
    private static final String APPLICATION = 
        "spring.config.location=classpath:/application-${spring.profiles.active:default}.yml";

    public static void main(String[] args) {
        new SpringApplicationBuilder(PrimaveraApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .properties(APPLICATION)
                .build()
                .run(args);
    }
}
```

### 5. Spring Boot 애플리케이션 시작 프로세스

```mermaid
flowchart TD
    start([SpringApplication.run]) --> createApp[SpringApplication 생성]
    createApp --> prepareEnv[환경 준비]
    prepareEnv --> createContext[ApplicationContext 생성]
    createContext --> prepareContext[컨텍스트 준비]
    prepareContext --> refreshContext[컨텍스트 새로고침]
    refreshContext --> callRunners[CommandLineRunner 실행]
    callRunners --> ready[애플리케이션 준비 완료]
```

**상세 단계:**
1. **환경 준비**: 프로파일, 속성 설정
2. **컨텍스트 생성**: ServletWebServerApplicationContext
3. **자동 구성 적용**: EnableAutoConfiguration 동작
4. **빈 등록 및 초기화**: IoC 컨테이너 구성
5. **웹 서버 시작**: 내장 Tomcat/Undertow 구동

## 🔧 실습 예제

### 핵심 애플리케이션 클래스

```java
@SpringBootApplication
public class PrimaveraApplication {
    
    // SpringApplicationBuilder를 통한 유연한 구성
    private static final String APPLICATION_CONFIG = 
        "spring.config.location=classpath:/application-${spring.profiles.active:default}.yml";
    
    public static void main(String[] args) {
        new SpringApplicationBuilder(PrimaveraApplication.class)
                .bannerMode(Banner.Mode.OFF)
                .properties(APPLICATION_CONFIG)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }
    
    // 커스텀 초기화 빈
    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            log.info("🌸 Primavera Application이 성공적으로 시작되었습니다!");
            log.info("📊 활성 프로파일: {}", 
                Arrays.toString(environment.getActiveProfiles()));
        };
    }
}
```

### 자동 구성 테스트

```java
@SpringBootTest
class AutoConfigurationTest {
    
    @Autowired
    private ApplicationContext context;
    
    @Test
    @DisplayName("Spring Boot 자동 구성이 정상적으로 동작하는지 확인")
    void testAutoConfiguration() {
        // DataSource 자동 구성 확인
        assertThat(context.getBean(DataSource.class)).isNotNull();
        
        // WebMvcConfigurer 자동 구성 확인
        assertThat(context.getBean(WebMvcConfigurer.class)).isNotNull();
        
        // 사용자 정의 빈 등록 확인
        assertThat(context.getBean(PrimaveraApplication.class)).isNotNull();
    }
}
```

## 🧪 테스트 전략

### 단위 테스트
- **@SpringBootTest**: 전체 애플리케이션 컨텍스트 로드
- **@TestConfiguration**: 테스트 전용 구성
- **@MockBean**: 특정 빈 모킹

### 통합 테스트
- **ApplicationContext 검증**: 빈 등록 상태 확인
- **자동 구성 검증**: 조건부 빈 생성 테스트
- **프로파일별 테스트**: 환경별 구성 검증

## 📖 참고 자료

### 공식 문서
- [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Framework Core](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html)
- [Auto-configuration Classes](https://docs.spring.io/spring-boot/docs/current/reference/html/auto-configuration-classes.html)

### 추가 학습 자료
- [Spring Boot Starters](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.build-systems.starters)
- [Creating Your Own Auto-configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.developing-auto-configuration)
- [SpringApplication](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/SpringApplication.html)

## 🚀 다음 단계

다음 Chapter에서는 **설정과 의존성 주입**을 학습합니다:
- `@ConfigurationProperties`를 통한 타입 안전한 설정
- Bean Scope와 라이프사이클 관리
- 프로파일별 환경 설정 전략

---

**🎓 학습 포인트**: Spring Boot의 자동 구성 메커니즘을 이해하면 프레임워크의 "마법" 같은 동작을 명확히 파악할 수 있습니다. 이는 문제 해결과 커스터마이징에 필수적인 지식입니다.