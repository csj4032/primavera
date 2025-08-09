# Primavera Preface - 경량 프레임워크

Spring Boot의 핵심 동작 원리를 이해하기 위해 직접 구현한 경량 프레임워크입니다. IoC 컨테이너, 의존성 주입, 컴포넌트 스캔 등 Spring Boot의 핵심 기능을 간소화하여 구현함으로써 프레임워크 내부 동작 원리를 학습할 수 있습니다.

## 학습 목표

- **IoC 컨테이너 이해**: Spring Boot의 ApplicationContext가 어떻게 동작하는지 학습
- **의존성 주입 메커니즘**: @Autowired가 내부적으로 어떻게 동작하는지 이해
- **어노테이션 처리**: 런타임 어노테이션 스캔과 리플렉션 활용법 학습
- **컴포넌트 스캔**: 패키지 탐색과 동적 클래스 로딩 원리 이해

## 프로젝트 구조

```
src/main/java/com/genius/primavera/lightweight/
├── annotations/                    # 커스텀 어노테이션
│   ├── PrimaveraComponent.java     # @Component 역할
│   ├── PrimaveraAutowired.java     # @Autowired 역할  
│   ├── PrimaveraConfiguration.java # @Configuration 역할
│   └── PrimaveraBean.java         # @Bean 역할
├── framework/                     # 프레임워크 핵심
│   ├── PrimaveraApplicationContext.java  # IoC 컨테이너
│   ├── PrimaveraApplication.java         # 애플리케이션 실행기
│   └── events/                          # 이벤트 시스템
├── interfaces/                    # 인터페이스 정의
│   └── PrimaveraApplicationRunner.java  # 시작 후 실행
└── example/                      # 데모 애플리케이션
    ├── services/                 # 비즈니스 로직
    ├── config/                   # 설정 클래스
    ├── runners/                  # 실행 후 로직
    └── PrimaveraLightweightDemo.java
```

## 주요 기능

### 1. 컴포넌트 자동 감지
```java
@PrimaveraComponent
public class GreetingService {
    public String sayHello(String name) {
        return "안녕하세요, " + name + "님!";
    }
}
```

### 2. 의존성 주입
```java
@PrimaveraComponent  
public class MessageService {
    @PrimaveraAutowired
    private GreetingService greetingService;
    
    public void processMessage(String name) {
        String greeting = greetingService.sayHello(name);
        log.info(greeting);
    }
}
```

### 3. 설정 기반 Bean 등록
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

### 4. 애플리케이션 시작 후 실행
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

## 기술 스택

| 기술 | 역할 | 구현 상태 |
|------|------|-----------|
| **Java Reflection** | 동적 클래스 로딩 및 인스턴스 생성 | ✅ |
| **Annotation Processing** | 런타임 어노테이션 처리 | ✅ |
| **Package Scanning** | 클래스패스 탐색 | ✅ |
| **Properties Loading** | application.properties 로드 | ✅ |
| **Event System** | 애플리케이션 이벤트 처리 | ✅ |
| **SLF4J Logging** | 구조화된 로깅 시스템 | ✅ |

## 실행 방법

### 1. 프로젝트 빌드
```bash
./gradlew :preface:build
```

### 2. 애플리케이션 실행
```bash
# Gradle을 통한 실행
./gradlew :preface:run

# 또는 직접 실행
java -cp preface/build/classes/java/main:preface/build/resources/main \
     com.genius.primavera.lightweight.example.PrimaveraLightweightDemo
```

### 3. 실행 결과
```
██████╗ ██████╗ ██╗███╗   ███╗ █████╗ ██╗   ██╗███████╗██████╗  █████╗ 
██╔══██╗██╔══██╗██║████╗ ████║██╔══██╗██║   ██║██╔════╝██╔══██╗██╔══██╗
██████╔╝██████╔╝██║██╔████╔██║███████║██║   ██║█████╗  ██████╔╝███████║
██╔═══╝ ██╔══██╗██║██║╚██╔╝██║██╔══██║╚██╗ ██╔╝██╔══╝  ██╔══██╗██╔══██║
██║     ██║  ██║██║██║ ╚═╝ ██║██║  ██║ ╚████╔╝ ███████╗██║  ██║██║  ██║
╚═╝     ╚═╝  ╚═╝╚═╝╚═╝     ╚═╝╚═╝  ╚═╝  ╚═══╝  ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝

:: Primavera Lightweight Framework ::                          (v1.0.0)

[INFO ] - Primavera 애플리케이션 시작 중...
[INFO ] - Primavera ApplicationContext 초기화 시작...
[INFO ] - Primavera ApplicationContext 초기화 완료! 등록된 Bean 수: 7
[INFO ] - 안녕하세요, 개발자님!
```

## 핵심 학습 포인트

### 1. IoC 컨테이너 구현
- Bean 정의 등록과 인스턴스 생성 과정
- 의존성 그래프 해결 알고리즘
- Bean 라이프사이클 관리
- 리플렉션을 활용한 동적 객체 생성

### 2. 어노테이션 처리 메커니즘
- `@Retention`, `@Target` 메타 어노테이션 활용
- 런타임 어노테이션 스캔 구현
- 리플렉션을 통한 어노테이션 정보 추출
- 어노테이션 기반 설정 처리

### 3. 컴포넌트 스캔 로직
- 클래스패스 탐색 알고리즘
- 디렉토리 구조 순회 및 필터링
- 동적 클래스 로딩과 인스턴스화
- 패키지 기반 컴포넌트 발견

### 4. 설정 외부화
- Properties 파일 파싱 및 로딩
- 환경 변수와 프로퍼티 통합
- 타입별 설정 값 변환
- 계층적 설정 구조 처리

## 테스트 실행

### 단위 테스트
```bash
# 모든 테스트 실행
./gradlew :preface:test

# 특정 테스트 클래스 실행
./gradlew :preface:test --tests "*PrimaveraApplicationContextTest"

# 특정 테스트 메서드 실행
./gradlew :preface:test --tests "*GreetingServiceTest.shouldSayHello"
```

### 테스트 커버리지
- **PrimaveraApplicationContext**: IoC 컨테이너 동작 검증
- **컴포넌트 스캔**: 어노테이션 기반 Bean 등록 검증
- **의존성 주입**: @PrimaveraAutowired 동작 검증
- **설정 처리**: @PrimaveraConfiguration 및 @PrimaveraBean 검증

## 주요 애너테이션

| 애너테이션 | Spring 대응 | 설명 |
|------------|-------------|------|
| `@PrimaveraComponent` | `@Component` | 컴포넌트 자동 스캔 대상 표시 |
| `@PrimaveraAutowired` | `@Autowired` | 의존성 자동 주입 |
| `@PrimaveraConfiguration` | `@Configuration` | 설정 클래스 표시 |
| `@PrimaveraBean` | `@Bean` | 메서드 기반 Bean 정의 |

## 확장 가능한 기능

현재 미구현된 기능들로 추가 학습 가능:
- **생성자 주입**: 필드 주입 외에 생성자 기반 주입
- **프로파일**: 환경별 Bean 조건부 등록
- **AOP**: 횡단 관심사 처리를 위한 프록시
- **웹 MVC**: 간단한 HTTP 요청 처리
- **스케줄링**: 작업 스케줄링 기능

## 활용 방법

### 1. Spring Boot 이해도 향상
- 실제 Spring Boot 학습 전 기본 개념 습득
- 프레임워크 내부 동작 원리 이해
- IoC/DI 개념의 실제 구현 경험

### 2. 디자인 패턴 학습
- Factory Method 패턴 (Bean 생성)
- Registry 패턴 (Bean 관리)  
- Observer 패턴 (이벤트 시스템)
- Template Method 패턴 (라이프사이클)

### 3. 고급 Java 기술 활용
- Reflection API 심화 활용
- Annotation Processing 실전 적용
- 클래스로더와 패키지 시스템 이해
- 동적 프록시와 바이트코드 조작 기초

이 경량 프레임워크는 Spring Boot의 복잡성을 제거하고 핵심 개념만을 구현하여, Spring Boot를 학습하기 전 기본기를 다지는 데 도움이 됩니다.