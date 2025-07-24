## hello

### Hystrix
Hystrix는 Netflix에서 개발한 장애 내성 및 지연 내성을 제공하는 라이브러리입니다. 주요 특징은 다음과 같습니다:

1. **서킷 브레이커 패턴**: 서비스 호출이 실패할 경우 서킷을 열어 추가 호출을 차단하고 빠른 실패 처리를 제공합니다.
2. **폴백 메커니즘**: 서비스 호출 실패 시 대체 로직을 실행할 수 있게 합니다.
3. **격리**: 서비스 간 호출을 격리하여 한 서비스의 장애가 전체 시스템에 영향을 주지 않도록 합니다.
4. **모니터링**: 실시간으로 서비스 호출 상태를 모니터링할 수 있는 대시보드를 제공합니다.

Spring Cloud에서는 `@HystrixCommand` 어노테이션을 통해 간편하게 Hystrix 기능을 사용할 수 있습니다.

```java
@HystrixCommand(fallbackMethod = "fallbackMethod")
public String serviceMethod() {
    // 원래 서비스 로직
}

public String fallbackMethod() {
    return "서비스 호출 실패 시 대체 응답";
}
```

### Openfeign
OpenFeign은 Netflix에서 개발하고 Spring Cloud에서 채택한 선언적 HTTP 클라이언트입니다. 주요 특징은 다음과 같습니다:

1. **인터페이스 기반 클라이언트**: 인터페이스 선언만으로 HTTP 클라이언트 구현체를 자동 생성합니다.
2. **RESTful API 통합**: REST API 호출을 위한 코드를 간소화합니다.
3. **Hystrix 통합**: 기본적으로 Hystrix와 통합되어 장애 허용 시스템을 구축할 수 있습니다.
4. **리본 통합**: 클라이언트 측 로드 밸런싱을 위한 Ribbon과 함께 작동합니다.

Spring Cloud에서는 `@FeignClient` 어노테이션을 사용하여 선언합니다.

```java
@FeignClient(name = "service-name", fallback = FallbackClass.class)
public interface ServiceClient {
    @GetMapping("/api/resource/{id}")
    ResponseEntity<Resource> getResource(@PathVariable("id") Long id);
}
```

### Turbine
Turbine은 여러 Hystrix 인스턴스의 스트림을 집계하는 도구입니다. 주요 특징은 다음과 같습니다:

1. **스트림 집계**: 여러 서비스 인스턴스에서 생성된 Hystrix 스트림을 하나로 집계합니다.
2. **클러스터링**: 서비스를 클러스터로 그룹화하여 모니터링할 수 있습니다.
3. **Hystrix Dashboard 통합**: 집계된 데이터를 Hystrix Dashboard에서 시각화할 수 있습니다.
4. **분산 시스템 모니터링**: 마이크로서비스 환경에서 전체 시스템의 상태를 모니터링하는 데 유용합니다.

Spring Cloud에서는 `@EnableTurbine` 어노테이션으로 Turbine을 활성화합니다.

```java
@SpringBootApplication
@EnableTurbine
public class TurbineApplication {
    public static void main(String[] args) {
        SpringApplication.run(TurbineApplication.class, args);
    }
}
```

### Spring Cloud Gateway
Spring Cloud Gateway는 API 게이트웨이 기능을 제공하는 Spring Cloud 컴포넌트입니다. 주요 특징은 다음과 같습니다:

1. **라우팅**: 클라이언트 요청을 적절한 마이크로서비스로 라우팅합니다.
2. **필터링**: 요청과 응답을 수정할 수 있는 필터 체인을 제공합니다.
3. **서킷 브레이커 통합**: Hystrix와 같은 서킷 브레이커와 통합됩니다.
4. **로드 밸런싱**: 클라이언트 측 로드 밸런싱을 지원합니다.
5. **보안**: 인증 및 권한 부여 필터를 적용할 수 있습니다.

```java
@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("service_route", r -> r.path("/service/**")
                .filters(f -> f.rewritePath("/service/(?<segment>.*)", "/${segment}")
                             .addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
                .uri("lb://SERVICE"))
            .build();
    }
}
```

### Eureka
Eureka는 Netflix에서 개발한 서비스 디스커버리 서버입니다. 주요 특징은 다음과 같습니다:

1. **서비스 등록**: 마이크로서비스가 시작될 때 자동으로 Eureka 서버에 등록됩니다.
2. **서비스 탐색**: 클라이언트가 서비스 이름으로 실제 서비스 인스턴스를 찾을 수 있습니다.
3. **상태 모니터링**: 등록된 서비스의 상태를 주기적으로 확인합니다.
4. **자가 보존 모드**: 네트워크 문제 발생 시 등록 정보를 보존하는 기능을 제공합니다.

```java
// Eureka 서버 설정
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

// Eureka 클라이언트 설정
@SpringBootApplication
@EnableDiscoveryClient
public class ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }
}
```

### Ribbon
Ribbon은 Netflix에서 개발한 클라이언트 측 로드 밸런서입니다. 주요 특징은 다음과 같습니다:

1. **클라이언트 로드 밸런싱**: 서버 측이 아닌 클라이언트 측에서 로드 밸런싱을 수행합니다.
2. **다양한 로드 밸런싱 알고리즘**: 라운드 로빈, 가중치 기반, 응답 시간 기반 등 여러 알고리즘을 지원합니다.
3. **Eureka 통합**: Eureka와 통합하여 동적으로 서비스 인스턴스를 발견합니다.
4. **실패 감지**: 서비스 호출 실패를 감지하고 자동으로 다른 인스턴스로 재시도합니다.

Spring Cloud에서는 `@LoadBalanced` 어노테이션을 사용하여 RestTemplate에 Ribbon 기능을 적용할 수 있습니다.

```java
@Configuration
public class RestTemplateConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

// 사용 예
@Service
public class ServiceClient {
    @Autowired
    private RestTemplate restTemplate;
    
    public Resource getResource(Long id) {
        // 서비스 이름으로 직접 호출 (실제 주소는 Ribbon이 해결)
        return restTemplate.getForObject("http://service-name/api/resource/" + id, Resource.class);
    }
}
```
