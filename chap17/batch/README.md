# Chapter 17 Batch - Spring Batch 대용량 데이터 처리 및 네이티브 이미지

Spring Boot 교육용 프로젝트 Primavera의 Chapter 17 배치 모듈입니다. Spring Batch를 사용한 대용량 데이터 처리와 GraalVM Native Image를 통한 성능 최적화를 학습합니다.

## 🎯 학습 목표

- **Spring Batch**: 대용량 배치 처리 아키텍처
- **GraalVM Native**: 네이티브 이미지 컴파일 및 성능 최적화
- **Elasticsearch 연동**: 검색 엔진과의 배치 데이터 동기화
- **CDC (Change Data Capture)**: 실시간 데이터 변경 감지
- **멀티모듈 활용**: 공통 도메인 모델 재사용

## 📁 프로젝트 구조

```
chap17/batch/
├── src/main/java/com/genius/primavera/
│   ├── ProductBatchApplication.java      # 메인 애플리케이션
│   └── batch/
│       ├── config/                       # 배치 설정
│       │   ├── BatchConfiguration.java       # Spring Batch 설정
│       │   ├── ProductIndexingJobConfig.java # 상품 인덱싱 잡 설정
│       │   ├── ElasticsearchConfiguration.java # Elasticsearch 설정
│       │   └── DebeziumEngineRunner.java     # CDC 엔진 설정
│       ├── controller/                   # REST API 컨트롤러
│       │   └── JobLauncherController.java    # 배치 잡 실행 API
│       ├── processor/                    # 데이터 처리기
│       │   └── ProductDocumentProcessor.java # 상품 문서 변환
│       ├── writer/                      # 데이터 작성기
│       │   └── ElasticsearchItemWriter.java # Elasticsearch 작성기
│       ├── service/                     # 서비스 레이어
│       │   └── ProductIndexingService.java  # 인덱싱 서비스
│       └── repository/                  # 데이터 접근 계층
│           ├── ProductRepository.java       # 상품 리포지터리
│           ├── CategoryRepository.java      # 카테고리 리포지터리
│           └── SellerRepository.java        # 판매자 리포지터리
├── src/main/resources/
│   ├── application.yml              # 메인 설정
│   ├── application-local.yml        # 로컬 환경 설정
│   └── logback-spring.xml          # 로깅 설정
└── build.gradle                    # GraalVM Native 설정 포함
```

## 🏗 아키텍처 특성

### 1. Spring Batch 아키텍처
```java
@Configuration
@EnableBatchProcessing
public class ProductIndexingJobConfig {
    
    @Bean
    public Job productIndexingJob(JobRepository jobRepository,
                                 Step productIndexingStep) {
        return new JobBuilder("productIndexingJob", jobRepository)
                .start(productIndexingStep)
                .build();
    }
    
    @Bean
    public Step productIndexingStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager) {
        return new StepBuilder("productIndexingStep", jobRepository)
                .<Product, ProductDocument>chunk(100, transactionManager)
                .reader(productReader())
                .processor(productProcessor())
                .writer(elasticsearchWriter())
                .build();
    }
}
```

### 2. GraalVM Native 최적화
```gradle
plugins {
    id 'org.graalvm.buildtools.native' version '0.10.3'
}

graalvmNative {
    binaries {
        main {
            imageName = 'product-batch-indexer'
            mainClass = 'com.genius.primavera.ProductBatchApplication'
            useFatJar = false
        }
    }
}
```

### 3. 멀티모듈 의존성
```gradle
dependencies {
    implementation project(':chap17:common')  // 공통 도메인 모델 사용
    implementation "org.springframework.boot:spring-boot-starter-batch"
    implementation "co.elastic.clients:elasticsearch-java:${elasticsearchVersion}"
}
```

## 🎯 핵심 기능

### 1. 대용량 데이터 배치 처리
- **Chunk 기반 처리**: 대용량 데이터를 청크 단위로 처리
- **Reader-Processor-Writer 패턴**: 데이터 읽기, 변환, 저장 분리
- **트랜잭션 관리**: 배치 처리 중 장애 복구

### 2. Elasticsearch 인덱싱
```java
@Component
public class ElasticsearchItemWriter implements ItemWriter<ProductDocument> {
    
    @Override
    public void write(Chunk<? extends ProductDocument> chunk) throws Exception {
        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
        
        for (ProductDocument doc : chunk) {
            bulkBuilder.operations(op -> op
                .index(idx -> idx
                    .index("products")
                    .id(doc.getId().toString())
                    .document(doc)
                )
            );
        }
        
        elasticsearchClient.bulk(bulkBuilder.build());
    }
}
```

### 3. CDC (Change Data Capture)
```java
@Component
public class DebeziumEngineRunner {
    
    @PostConstruct
    public void startDebeziumEngine() {
        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create()
            .using(connectorConfig())
            .notifying(this::handleChangeEvent)
            .build();
            
        executor.execute(engine);
    }
    
    private void handleChangeEvent(ChangeEvent<String, String> event) {
        // 실시간 데이터 변경 처리
    }
}
```

### 4. RESTful 배치 제어
```java
@RestController
@RequestMapping("/batch")
public class JobLauncherController {
    
    @PostMapping("/products/index")
    public ResponseEntity<String> startProductIndexing() {
        JobParameters params = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
            
        jobLauncher.run(productIndexingJob, params);
        return ResponseEntity.ok("Batch job started");
    }
}
```

## 🛠 기술 스택

### 핵심 기술
- **Java**: 21 (GraalVM Native 지원)
- **Spring Boot**: 3.3.6
- **Spring Batch**: 배치 처리 프레임워크
- **GraalVM Native**: 네이티브 이미지 컴파일
- **Elasticsearch**: 검색 엔진 연동

### 데이터베이스
- **MariaDB**: 11.4.7 (소스 데이터)
- **Elasticsearch**: 검색 인덱스 대상

### 테스트
- **TestContainers**: MariaDB, Elasticsearch 통합 테스트
- **Spring Batch Test**: 배치 테스트 지원

## 🚀 실행 방법

### 1. 일반 실행
```bash
# 배치 애플리케이션 실행
./gradlew :chap17:batch:bootRun

# 로컬 환경으로 실행
./gradlew :chap17:batch:bootRun -Dspring.profiles.active=local
```

### 2. Native 이미지 빌드 및 실행
```bash
# Native 이미지 빌드
./gradlew :chap17:batch:nativeCompile

# Native 이미지 실행 (빠른 시작 시간)
./chap17/batch/build/native/nativeCompile/product-batch-indexer
```

### 3. 배치 잡 실행 API
```bash
# 상품 인덱싱 배치 잡 시작
curl -X POST http://localhost:8080/batch/products/index

# 배치 잡 상태 확인
curl -X GET http://localhost:8080/batch/jobs/status
```

## 📋 테스트 실행

### 통합 테스트
```bash
# 전체 테스트 실행 (TestContainers 사용)
./gradlew :chap17:batch:test

# Elasticsearch 연동 테스트
./gradlew :chap17:batch:test --tests "*ElasticsearchIntegrationTest"

# 배치 성능 테스트
./gradlew :chap17:batch:test --tests "*PerformanceTest"
```

### Native 테스트
```bash
# Native 이미지 테스트
./gradlew :chap17:batch:nativeTest
```

## 🎓 핵심 학습 포인트

### 1. Spring Batch 패턴
- **Job과 Step**: 배치 작업의 논리적 구조
- **Chunk 처리**: 대용량 데이터의 효율적 처리
- **ItemReader/Processor/Writer**: 배치 처리의 핵심 컴포넌트

### 2. 성능 최적화
```java
@StepScope
@Bean
public JpaPagingItemReader<Product> productReader() {
    return new JpaPagingItemReaderBuilder<Product>()
        .name("productReader")
        .entityManagerFactory(entityManagerFactory)
        .pageSize(100)  // 페이지 크기 최적화
        .queryString("SELECT p FROM Product p WHERE p.status = 'ACTIVE'")
        .build();
}
```

### 3. GraalVM Native 이미지
- **AOT 컴파일**: Ahead-of-Time 컴파일로 빠른 시작 시간
- **메모리 효율성**: 낮은 메모리 사용량
- **반사 제한**: 컴파일 타임에 리플렉션 정보 확정

### 4. 멀티모듈 아키텍처
```gradle
dependencies {
    implementation project(':chap17:common')  // 공통 모듈 재사용
}
```

## 📚 주요 애너테이션

### Spring Batch
- `@EnableBatchProcessing`: Batch 설정 활성화
- `@JobScope`, `@StepScope`: 배치 스코프 설정
- `@AfterJob`, `@BeforeJob`: 잡 생명주기 콜백

### Native 설정
- `@RegisterReflectionForBinding`: Native 이미지를 위한 리플렉션 등록
- `@NativeHint`: Native 컴파일 힌트 제공

## 🔧 배치 모니터링

### 1. 배치 실행 메트릭
```java
@Component
public class BatchMetrics {
    
    @EventListener
    public void onJobExecution(JobExecutionEvent event) {
        meterRegistry.counter("batch.job.executions",
            "job", event.getJobExecution().getJobInstance().getJobName(),
            "status", event.getJobExecution().getStatus().toString()
        ).increment();
    }
}
```

### 2. 로그 기반 모니터링
```yaml
logging:
  level:
    org.springframework.batch: DEBUG
    com.genius.primavera.batch: INFO
```

## 🔄 다음 단계

1. **chap17:streaming** - 실시간 스트리밍 처리로 CDC 이벤트 활용
2. **chap18** - 마이크로서비스에서의 배치 처리 분산
3. **운영 환경 배포** - Kubernetes에서의 배치 잡 스케줄링

## 📖 관련 문서

- [Spring Batch Documentation](https://spring.io/projects/spring-batch)
- [GraalVM Native Image](https://www.graalvm.org/native-image/)
- [Elasticsearch Java Client](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/)
- [Debezium CDC](https://debezium.io/documentation/)