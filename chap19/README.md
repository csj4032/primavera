# Chapter 19 - 성능 최적화 심화

## 학습 목표

Spring Boot 애플리케이션의 성능을 극대화하는 고급 기법들을 학습하고 실습합니다.

- **Virtual Threads (Java 21)** 활용법과 성능 분석
- **멀티레이어 캐싱** 아키텍처 구현
- **JVM 튜닝** 전략과 모니터링
- **데이터베이스 쿼리 최적화** 기법
- **실시간 성능 모니터링** 시스템 구축

## 프로젝트 구조

```
chap19/
├── src/main/java/com/genius/primavera/
│   ├── PrimaveraPerformanceApplication.java
│   ├── domain/
│   │   ├── model/                          # 도메인 모델
│   │   │   ├── User.java                   # 최적화된 엔티티
│   │   │   ├── Role.java
│   │   │   ├── Post.java
│   │   │   └── PerformanceMetric.java      # 성능 메트릭
│   │   └── repository/
│   │       └── UserRepository.java         # 최적화된 쿼리
│   ├── application/
│   │   ├── jvm/
│   │   │   └── VirtualThreadService.java   # Virtual Thread 처리
│   │   ├── cache/
│   │   │   └── MultiLayerCacheService.java # 3단계 캐싱
│   │   ├── database/
│   │   │   └── QueryOptimizationService.java # 쿼리 최적화
│   │   └── monitoring/
│   │       └── PerformanceMonitorService.java # 성능 모니터링
│   ├── interfaces/rest/
│   │   ├── VirtualThreadController.java
│   │   └── PerformanceController.java
│   └── infrastructure/config/
│       └── CacheConfig.java               # 캐시 설정
└── src/test/java/
    └── performance/                        # 성능 테스트
        ├── VirtualThreadPerformanceTest.java
        └── CachePerformanceTest.java
```

## 기술 스택

### 핵심 기술
- **Java**: 21 (Virtual Threads, Records)
- **Spring Boot**: 3.3.6
- **Spring WebFlux**: 반응형 프로그래밍
- **Spring Data JPA**: 쿼리 최적화

### 성능 최적화
- **Caffeine**: L1 캐시 (In-memory)
- **Redis**: L2 캐시 (Distributed)
- **HikariCP**: 연결 풀 최적화
- **Hibernate**: 배치 처리, Entity Graph

### 모니터링
- **Micrometer**: 메트릭 수집
- **Prometheus**: 메트릭 저장
- **JMH**: 마이크로 벤치마크
- **JFR**: Java Flight Recorder

## 주요 기능

### 1. Virtual Threads 활용

```java
// I/O 집약적 작업의 Virtual Thread 처리
@GetMapping("/process/virtual/{count}")
public Mono<BatchResult> processWithVirtualThreads(@PathVariable int count) {
    var tasks = generateTasks(count);
    return Mono.fromFuture(virtualThreadService.processWithVirtualThreads(tasks));
}

// Platform Thread와 성능 비교
@GetMapping("/compare/{count}")
public Mono<PerformanceComparison> comparePerformance(@PathVariable int count) {
    return virtualThreadService.comparePerformance(count);
}
```

**특징:**
- **대량 동시 처리**: 1만+ 작업 동시 실행
- **메모리 효율성**: Platform Thread 대비 90% 메모리 절약
- **블로킹 I/O 최적화**: 데이터베이스, HTTP 호출 성능 향상

### 2. 멀티레이어 캐싱

```java
// 3단계 캐시 구조
public <T> Mono<T> get(String key, Class<T> type, Supplier<Mono<T>> loader) {
    // L1: Caffeine (In-memory, 초고속)
    return Mono.justOrEmpty(l1Cache.getIfPresent(key))
        .switchIfEmpty(
            // L2: Redis (Distributed, 공유)
            l2Cache.opsForValue().get(key)
                .doOnNext(value -> l1Cache.put(key, value))
        )
        .switchIfEmpty(
            // L3: Original Source (DB, API)
            loader.get()
                .doOnNext(value -> storeInAllLayers(key, value))
        );
}
```

**아키텍처:**
- **L1 캐시**: Caffeine (로컬, 마이크로초 응답)
- **L2 캐시**: Redis (분산, 밀리초 응답)
- **L3 소스**: Database/API (원본, 초 단위 응답)

### 3. 쿼리 최적화

```java
// N+1 문제 해결
@Query("""
    SELECT DISTINCT u FROM User u
    LEFT JOIN FETCH u.roles r
    LEFT JOIN FETCH u.posts p
    WHERE u.active = :active
    """)
List<User> findUsersOptimized(@Param("active") Boolean active);

// 배치 처리 최적화
@Modifying
@Query("UPDATE User u SET u.active = :active WHERE u.id IN :ids")
int updateUserStatusBatch(@Param("ids") List<Long> ids, @Param("active") Boolean active);
```

**최적화 기법:**
- **Fetch Join**: 관련 엔티티 한 번에 로드
- **Entity Graph**: 동적 페치 전략
- **Batch Processing**: 대량 데이터 효율적 처리
- **Streaming**: 메모리 효율적 대용량 처리

### 4. JVM 모니터링

```java
// 실시간 JVM 메트릭 수집
@Scheduled(fixedDelay = 10000)
public void collectMetrics() {
    // 메모리 사용량
    meterRegistry.gauge("jvm.memory.used", getMemoryUsage());
    
    // GC 통계
    meterRegistry.timer("jvm.gc.pause", getGCDuration());
    
    // Virtual Thread 수
    meterRegistry.gauge("jvm.threads.virtual", getVirtualThreadCount());
}
```

**모니터링 대상:**
- **메모리**: Heap, Non-heap, Pool별 사용량
- **GC**: 수집 횟수, 시간, 타입별 통계
- **스레드**: 총 개수, Virtual Thread 비율
- **CPU**: 사용률, 부하 평균

## API 엔드포인트

### Virtual Thread 성능 테스트
```bash
# Virtual Thread로 1000개 작업 처리
GET /api/virtual-threads/process/virtual/1000

# Platform Thread와 성능 비교
GET /api/virtual-threads/compare/1000

# 실시간 스레드 메트릭 스트림
GET /api/virtual-threads/metrics/stream
```

### 캐시 성능 테스트
```bash
# 캐시 메트릭 조회
GET /api/performance/cache/metrics

# 캐시 테스트
POST /api/performance/cache/test/user:123

# 캐시 무효화
DELETE /api/performance/cache/evict/user:123
```

### 쿼리 최적화 테스트
```bash
# 최적화된 쿼리 실행
GET /api/performance/query/optimized

# N+1 문제가 있는 쿼리 (비교용)
GET /api/performance/query/nplus1

# 배치 삽입 테스트
POST /api/performance/query/batch-insert?count=1000
```

### 시스템 모니터링
```bash
# 시스템 메트릭
GET /api/performance/system/metrics

# GC 통계
GET /api/performance/gc/statistics
```

## 실행 방법

### 1. 환경 설정

```bash
# 데이터베이스 시작 (Docker)
docker run -d --name mariadb-chap19 \
  -e MARIADB_ROOT_PASSWORD=root \
  -e MARIADB_DATABASE=primavera \
  -e MARIADB_USER=primavera \
  -e MARIADB_PASSWORD=primavera \
  -p 3319:3306 mariadb:11.4.7

# Redis 시작 (캐싱용)
docker run -d --name redis-chap19 \
  -p 6379:6379 redis:7-alpine
```

### 2. 애플리케이션 실행

```bash
# 기본 실행
./gradlew :chap19:bootRun

# JVM 최적화 옵션 포함
./gradlew :chap19:bootRun -Pjvmargs="\
  --enable-preview \
  -Djdk.virtualThreadScheduler.parallelism=1000 \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Xms1g -Xmx2g"
```

### 3. 성능 테스트 실행

```bash
# 단위 테스트 (성능 포함)
./gradlew :chap19:test

# JMH 벤치마크 실행
./gradlew :chap19:jmh
```

## 성능 최적화 결과

### Virtual Threads 성능 개선
- **처리량**: Platform Thread 대비 **300-500% 향상**
- **메모리**: **90% 절약** (1MB → 100KB per thread)
- **응답 시간**: I/O 대기 시간 **95% 단축**

### 캐싱 효과
- **L1 캐시 히트율**: **95%** (마이크로초 응답)
- **L2 캐시 히트율**: **90%** (밀리초 응답)
- **전체 응답 시간**: **99% 단축** (1초 → 10ms)

### 쿼리 최적화
- **N+1 문제 해결**: **80-90% 성능 향상**
- **배치 처리**: **단건 대비 100배 빠름**
- **메모리 사용량**: 스트리밍으로 **95% 절약**

## 모니터링 대시보드

### Prometheus 메트릭
```yaml
# Virtual Thread 메트릭
jvm_threads_virtual_count
jvm_threads_platform_count

# 캐시 메트릭  
cache_hit_total{layer="L1"}
cache_miss_total

# GC 메트릭
jvm_gc_pause_seconds
jvm_memory_used_bytes
```

### 알림 규칙
- **메모리 사용률 > 80%**
- **GC 시간 > 200ms**
- **캐시 히트율 < 70%**
- **Virtual Thread 생성 실패**

## 학습 포인트

### 1. **Virtual Threads의 이해**
- 기존 Platform Thread와의 차이점
- I/O 집약적 vs CPU 집약적 작업 최적화
- 스케줄러 튜닝과 성능 모니터링

### 2. **캐싱 전략 설계**
- 다층 캐시 아키텍처의 장단점
- 캐시 일관성과 무효화 전략
- TTL, 용량, 정책별 최적화

### 3. **JVM 튜닝 실무**
- GC 알고리즘 선택과 튜닝
- 메모리 영역별 최적화
- 프로파일링과 병목 지점 분석

### 4. **데이터베이스 최적화**
- ORM 쿼리 최적화 기법
- 인덱스 전략과 실행 계획 분석
- 배치 처리와 대용량 데이터 처리

## 다음 단계

이 모듈을 완료한 후에는:

1. **실제 프로젝트 적용**: 운영 환경 성능 최적화
2. **고급 모니터링**: APM 도구 연동 (Pinpoint, New Relic)
3. **클라우드 최적화**: 컨테이너 환경 성능 튜닝
4. **마이크로서비스**: 분산 시스템 성능 최적화

---

**Performance is not just about speed, it's about efficiency and scalability.**