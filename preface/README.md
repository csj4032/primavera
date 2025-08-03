# 🌸 Primavera Preface

## 📚 목차

1. [Primavera 경량 프레임워크](#-primavera-경량-프레임워크)
2. [다음 단계](#-다음-단계)

---

## 🌸 Primavera 경량 프레임워크

### 개요

Spring Boot의 동작 원리를 이해하기 위해 만든 경량 프레임워크입니다. 
Spring Boot의 핵심 기능들을 간단하게 구현하여 다음과 같은 학습 목표를 달성할 수 있습니다:

- IoC(Inversion of Control) 컨테이너의 동작 원리 이해
- 의존성 주입(Dependency Injection) 메커니즘 학습
- 어노테이션 기반 설정의 작동 방식 파악
- 컴포넌트 스캔과 Bean 라이프사이클 이해

### 🔄 최신 업데이트 - 로깅 시스템 개선

**System.out.println을 SLF4J 로깅으로 전환 완료:**

#### 변경된 파일들:
- `LifecycleService`: Bean 라이프사이클 로그를 구조화된 로깅으로 개선
- `MessageService`: 메시지 처리 로그를 적절한 로그 레벨로 분류
- `ApplicationStartupRunner`: 애플리케이션 시작 정보를 로그로 기록
- `PrimaveraApplication`: 배너 출력을 로그 시스템으로 통합

#### 로깅 개선 효과:
- **구조화된 로깅**: 파라미터화된 메시지로 성능 향상
- **로그 레벨 분류**: INFO, DEBUG 등 적절한 레벨 적용  
- **운영 환경 최적화**: 콘솔 출력 대신 로그 파일로 기록 가능
- **모니터링 친화적**: 로그 수집 및 분석 도구와 연동 용이

### 🚀 빠른 시작

#### 1. 데모 애플리케이션 실행

```bash
# 프로젝트 루트에서 실행
./gradlew :preface:run

# 또는 직접 클래스 실행
./gradlew :preface:classes
java -cp preface/build/classes/java/main:preface/build/resources/main \
     com.genius.primavera.lightweight.example.PrimaveraLightweightDemo
```

#### 2. 실행 결과 확인

성공적으로 실행되면 다음과 같은 출력을 볼 수 있습니다:

```
██████╗ ██████╗ ██╗███╗   ███╗ █████╗ ██╗   ██╗███████╗██████╗  █████╗ 
██╔══██╗██╔══██╗██║████╗ ████║██╔══██╗██║   ██║██╔════╝██╔══██╗██╔══██╗
██████╔╝██████╔╝██║██╔████╔██║███████║██║   ██║█████╗  ██████╔╝███████║
██╔═══╝ ██╔══██╗██║██║╚██╔╝██║██╔══██║╚██╗ ██╔╝██╔══╝  ██╔══██╗██╔══██║
██║     ██║  ██║██║██║ ╚═╝ ██║██║  ██║ ╚████╔╝ ███████╗██║  ██║██║  ██║
╚═╝     ╚═╝  ╚═╝╚═╝╚═╝     ╚═╝╚═╝  ╚═╝  ╚═══╝  ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝

:: Primavera Lightweight Framework ::                          (v1.0.0)

🌸 Primavera 애플리케이션 시작 중...
🌸 Primavera ApplicationContext 초기화 시작...
🌸 Primavera ApplicationContext 초기화 완료! 등록된 Bean 수: 7
```

### 🏗️ 아키텍처

#### 핵심 컴포넌트

```
src/main/java/com/genius/primavera/lightweight/
├── annotations/           # 커스텀 어노테이션들
│   ├── PrimaveraComponent.java        # @Component 역할
│   ├── PrimaveraAutowired.java        # @Autowired 역할  
│   ├── PrimaveraConfiguration.java    # @Configuration 역할
│   └── PrimaveraBean.java            # @Bean 역할
├── framework/            # 프레임워크 핵심 로직
│   ├── PrimaveraApplicationContext.java   # IoC 컨테이너
│   ├── PrimaveraApplication.java          # 애플리케이션 실행기
│   └── events/                            # 이벤트 시스템
├── interfaces/           # 인터페이스 정의
│   └── PrimaveraApplicationRunner.java    # 시작 후 실행 인터페이스
└── example/             # 데모 애플리케이션
    ├── services/        # 비즈니스 로직
    ├── config/          # 설정 클래스  
    ├── runners/         # 실행 후 로직
    └── PrimaveraLightweightDemo.java  # 메인 클래스
```

### 📝 사용법

#### 1. 컴포넌트 등록

```java
@PrimaveraComponent
public class GreetingService {
    public String sayHello(String name) {
        return "🌸 안녕하세요, " + name + "님!";
    }
}
```

#### 2. 의존성 주입

```java
@PrimaveraComponent  
public class MessageService {
    
    @PrimaveraAutowired
    private GreetingService greetingService;
    
    public void processMessage(String name) {
        String greeting = greetingService.sayHello(name);
        System.out.println(greeting);
    }
}
```

#### 3. 설정 클래스

```java
@PrimaveraConfiguration
public class AppConfiguration {
    
    @PrimaveraBean
    public String applicationName() {
        return "Primavera Demo App";
    }
    
    @PrimaveraBean("maxUsers")
    public Integer maxUserCount() {
        return 100;
    }
}
```

#### 4. 애플리케이션 시작 후 실행

```java
@PrimaveraComponent
public class StartupRunner implements PrimaveraApplicationRunner {
    
    @PrimaveraAutowired
    private MessageService messageService;
    
    @Override
    public void run() throws Exception {
        messageService.processMessage("개발자");
    }
}
```

#### 5. 메인 애플리케이션

```java
public class MyApplication {
    public static void main(String[] args) {
        PrimaveraApplication.run(MyApplication.class, args);
    }
}
```

### ⚡ 주요 기능

| 기능 | 설명 | 구현 상태 |
|------|------|-----------|
| **컴포넌트 스캔** | `@PrimaveraComponent` 자동 감지 | ✅ |
| **의존성 주입** | `@PrimaveraAutowired` 필드 주입 | ✅ |
| **설정 기반 Bean** | `@PrimaveraConfiguration` + `@PrimaveraBean` | ✅ |
| **ApplicationRunner** | 시작 후 로직 실행 | ✅ |
| **환경 설정** | `application.properties` 로드 | ✅ |
| **이벤트 시스템** | 애플리케이션 이벤트 처리 | ✅ |
| **생성자 주입** | 생성자 기반 의존성 주입 | 🚧 |
| **프로파일** | 환경별 설정 분리 | 🚧 |

### 🧪 테스트

#### 테스트 실행

```bash
# 모든 테스트 실행
./gradlew :preface:test

# 특정 테스트 클래스 실행
./gradlew :preface:test --tests "*PrimaveraApplicationContextTest"

# 특정 테스트 메서드 실행  
./gradlew :preface:test --tests "*GreetingServiceTest.shouldSayHello"
```

#### 테스트 커버리지

- **PrimaveraApplicationContext**: Bean 등록, 의존성 주입 테스트
- **GreetingService**: 비즈니스 로직 단위 테스트
- **통합 테스트**: 전체 애플리케이션 라이프사이클 테스트

### 🎯 학습 포인트

#### 1. IoC 컨테이너 이해

`PrimaveraApplicationContext`를 통해 다음을 학습할 수 있습니다:

- Bean 정의 등록과 인스턴스 생성
- 의존성 그래프 해결
- Bean 라이프사이클 관리
- 리플렉션을 활용한 동적 객체 생성

#### 2. 어노테이션 처리

커스텀 어노테이션 구현을 통해 다음을 이해할 수 있습니다:

- `@Retention`, `@Target` 메타 어노테이션
- 런타임 어노테이션 스캔
- 리플렉션을 통한 어노테이션 정보 추출

#### 3. 컴포넌트 스캔

패키지 스캔 로직을 통해 다음을 학습할 수 있습니다:

- 클래스패스 탐색
- 디렉토리 구조 순회
- 동적 클래스 로딩

#### 4. 설정 외부화

`application.properties` 처리를 통해 다음을 이해할 수 있습니다:

- 프로퍼티 파일 파싱
- 환경 변수 통합
- 설정 값 타입 변환

### 🚧 확장 가능한 기능들

현재 구현되지 않았지만, 추가로 구현해볼 만한 기능들:

1. **생성자 주입**: 필드 주입 외에 생성자 주입 지원
2. **프로파일**: `@Profile` 어노테이션으로 환경별 Bean 관리
3. **조건부 Bean**: `@ConditionalOnProperty` 같은 조건부 등록
4. **AOP**: 횡단 관심사 처리를 위한 프록시 생성
5. **웹 MVC**: 간단한 HTTP 요청 처리
6. **타입 세이프 설정**: `@ConfigurationProperties` 스타일
7. **Bean Validation**: JSR-303 검증 통합
8. **스케줄링**: `@Scheduled` 작업 스케줄링

---

## 🚀 다음 단계

### 추가 학습 자료

1. **Spring Boot 공식 문서**: [spring.io](https://spring.io/projects/spring-boot)
2. **Primavera 프로젝트**: 다른 챕터들을 통해 점진적으로 Spring Boot 학습 
3. **디자인 패턴**: GoF 디자인 패턴과 Spring에서의 활용