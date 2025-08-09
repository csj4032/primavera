# Chapter 16: 파일 처리, S3, Excel, 모니터링

Spring Boot에서 클라우드 스토리지와 고급 파일 처리 시스템을 학습하는 실무 중심 모듈입니다. AWS S3 연동, Excel/CSV 대용량 처리, Spring Boot Actuator 모니터링, Sentry 에러 추적을 통한 완전한 파일 처리 워크플로우를 구현합니다.

## 🎯 고급 학습 목표

- **AWS S3 Integration**: 클라우드 객체 스토리지를 활용한 파일 관리 시스템
- **Advanced File Processing**: Excel, CSV, 대용량 파일의 효율적 처리
- **Spring Boot Actuator**: 애플리케이션 상태 모니터링과 메트릭 수집
- **Error Tracking**: Sentry를 통한 실시간 에러 모니터링과 알람
- **Chain of Responsibility**: 파일 검증 시스템의 체계적 구현
- **Factory Pattern**: 파일 타입별 처리 전략 패턴
- **Template Method**: Excel 처리 템플릿과 함수형 프로그래밍

## 📁 프로젝트 구조

```
chap16/
├── src/main/java/com/genius/primavera/
│   ├── FileProcessingMonitoringApplication.java # 메인 애플리케이션
│   ├── application/                             # 비즈니스 로직 계층
│   │   ├── aws/                                 # AWS S3 서비스
│   │   │   ├── S3FileService.java               # S3 파일 업로드/다운로드
│   │   │   ├── S3FileServiceImpl.java           # S3 서비스 구현
│   │   │   └── S3FileMetadata.java              # S3 파일 메타데이터
│   │   ├── factory/                             # Factory Pattern
│   │   │   ├── AbstractResponseFactory.java    # 추상 팩토리
│   │   │   ├── ExcelTypeFile.java              # Excel 파일 팩토리
│   │   │   ├── SizeZeroFile.java               # 빈 파일 팩토리
│   │   │   └── UnknownFile.java                # 알 수 없는 파일 팩토리
│   │   ├── template/                            # Template Method Pattern
│   │   │   ├── ExcelImportTemplate.java        # Excel 처리 템플릿
│   │   │   └── FinancialTemplate.java          # 금융 데이터 템플릿
│   │   ├── validator/                           # Chain of Responsibility
│   │   │   ├── Validator.java                  # 검증기 인터페이스
│   │   │   ├── FileSizeValidator.java          # 파일 크기 검증
│   │   │   ├── MediaTypeValidation.java        # 미디어 타입 검증
│   │   │   ├── NullValidator.java              # Null 검증
│   │   │   └── VersionValidation.java          # 버전 검증
│   │   ├── ExcelImportService.java             # Excel 가져오기 서비스
│   │   └── ExcelImportServiceImpl.java         # 서비스 구현
│   ├── domain/                                  # 도메인 모델과 비즈니스 규칙
│   │   ├── ExcelImportRequest.java              # Excel 가져오기 요청
│   │   ├── ExcelImportResponse.java             # Excel 가져오기 응답
│   │   ├── Financial.java                       # 금융 데이터 모델
│   │   ├── KakaoTalkChat.java                  # 카카오톡 채팅 데이터
│   │   ├── Person.java                         # 개인정보 모델
│   │   ├── Primavera.java                      # Primavera 도메인 객체
│   │   └── repository/
│   │       └── KakaoTalkChatRepository.java    # 채팅 데이터 저장소
│   ├── infrastructure/                          # 인프라스트럭처 계층
│   │   ├── ApplicationComponent.java            # 애플리케이션 컴포넌트
│   │   ├── PrimaveraConfiguration.java          # 메인 설정
│   │   ├── PrimaveraRestExceptionHandler.java   # 글로벌 예외 처리
│   │   ├── aws/                                 # AWS 설정
│   │   │   ├── AwsProperties.java               # AWS 속성 설정
│   │   │   ├── S3Configuration.java             # S3 설정
│   │   │   └── S3Properties.java               # S3 속성
│   │   └── sentry/                              # Sentry 모니터링
│   │       ├── EnableSentry.java                # Sentry 활성화 애너테이션
│   │       ├── SentryAutoConfiguration.java     # Sentry 자동 설정
│   │       └── SentryProperties.java            # Sentry 속성
│   └── interfaces/                              # 웹 인터페이스 계층
│       ├── ExcelImportController.java           # Excel 업로드 API
│       ├── S3FileController.java                # S3 파일 관리 API
│       └── TypeController.java                  # 파일 타입 관리 API
└── src/main/resources/
    ├── application.yml                          # 기본 설정
    ├── application-local.yml                   # 로컬 환경 설정
    └── static/                                  # 정적 리소스
```

## 🔧 고급 기술 기능

### 1. AWS S3 클라우드 스토리지 통합

#### S3 파일 서비스 구현
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileServiceImpl implements S3FileService {
    
    private final S3Client s3Client;
    private final S3Properties s3Properties;
    
    @Override
    public S3FileMetadata uploadFile(String key, MultipartFile file) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .metadata(Map.of(
                    "original-filename", file.getOriginalFilename(),
                    "upload-timestamp", Instant.now().toString()
                ))
                .build();
            
            s3Client.putObject(request, RequestBody.fromInputStream(
                file.getInputStream(), file.getSize()));
            
            return S3FileMetadata.builder()
                .key(key)
                .bucketName(s3Properties.getBucketName())
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .uploadedAt(Instant.now())
                .s3Url(generateS3Url(key))
                .build();
                
        } catch (IOException e) {
            throw new StorageException("S3 파일 업로드 실패: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<InputStream> downloadFile(String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Properties.getBucketName())
                .key(key)
                .build();
                
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            return Optional.of(response);
            
        } catch (NoSuchKeyException e) {
            log.warn("S3에서 파일을 찾을 수 없음: {}", key);
            return Optional.empty();
        } catch (S3Exception e) {
            throw new StorageException("S3 파일 다운로드 실패: " + e.getMessage(), e);
        }
    }
    
    private String generateS3Url(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
            s3Properties.getBucketName(),
            s3Properties.getRegion(),
            key);
    }
}
```

#### S3 설정 Properties
```java
@ConfigurationProperties(prefix = "aws.s3")
@Data
@Component
public class S3Properties {
    
    private String bucketName;
    private String region = "ap-northeast-2";
    private String endpoint;
    private boolean pathStyleAccess = false;
    
    @Value("${spring.cloud.aws.credentials.access-key:#{null}}")
    private String accessKey;
    
    @Value("${spring.cloud.aws.credentials.secret-key:#{null}}")
    private String secretKey;
}
```

### 2. Chain of Responsibility 파일 검증 시스템

#### 검증기 인터페이스와 구현체들
```java
public interface Validator {
    boolean validate(ExcelImportRequest request);
}

@Slf4j
public class FileSizeValidator implements Validator {
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    @Override
    public boolean validate(ExcelImportRequest request) {
        log.info("파일 크기 검증 시작");
        
        try {
            long fileSize = request.getFile().getSize();
            boolean isValid = fileSize > 0 && fileSize <= MAX_FILE_SIZE;
            
            log.info("파일 크기: {} bytes, 검증 결과: {}", fileSize, isValid);
            return isValid;
            
        } catch (Exception e) {
            log.error("파일 크기 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}

@Slf4j
public class MediaTypeValidation implements Validator {
    
    private static final Set<String> ALLOWED_MEDIA_TYPES = Set.of(
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-excel",
        "text/csv"
    );
    
    @Override
    public boolean validate(ExcelImportRequest request) {
        log.info("미디어 타입 검증 시작");
        
        try (InputStream inputStream = request.getInputStream()) {
            Tika tika = new Tika();
            String detectedType = tika.detect(inputStream);
            
            boolean isValid = ALLOWED_MEDIA_TYPES.contains(detectedType);
            log.info("감지된 미디어 타입: {}, 검증 결과: {}", detectedType, isValid);
            
            return isValid;
            
        } catch (IOException e) {
            log.error("미디어 타입 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}
```

#### 검증 체인 구성
```java
@Service
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService {
    
    private final Map<String, List<Validator>> validatorGroups;
    
    @Override
    public ExcelImportResponse processExcel(ExcelImportRequest request) {
        // 검증 체인 실행
        boolean isValid = validatorGroups.get("fileValidation")
            .stream()
            .allMatch(validator -> validator.validate(request));
            
        if (!isValid) {
            return ExcelImportResponse.builder()
                .success(false)
                .errorMessage("파일 검증 실패")
                .build();
        }
        
        // 파일 처리 로직
        return processValidFile(request);
    }
}

@Configuration
public class ValidatorConfiguration {
    
    @Bean
    public Map<String, List<Validator>> validatorGroups() {
        Map<String, List<Validator>> groups = new HashMap<>();
        
        groups.put("fileValidation", List.of(
            new NullValidator(),
            new FileSizeValidator(),
            new MediaTypeValidation()
        ));
        
        groups.put("securityValidation", List.of(
            new VirusScanner(),
            new MaliciousContentDetector()
        ));
        
        return groups;
    }
}
```

### 3. Factory Pattern 파일 처리 전략

#### 추상 팩토리와 구현체들
```java
public abstract class AbstractResponseFactory {
    
    protected ExcelImportRequest request;
    
    protected AbstractResponseFactory(ExcelImportRequest request) {
        this.request = request;
    }
    
    public abstract ExcelImportResponse createResponse();
    
    protected long getFileSize() {
        return request.getFile().getSize();
    }
    
    protected String getMediaType() {
        try (InputStream inputStream = request.getInputStream()) {
            return new Tika().detect(inputStream);
        } catch (IOException e) {
            return "unknown";
        }
    }
}

public class ExcelTypeFile extends AbstractResponseFactory {
    
    public ExcelTypeFile(ExcelImportRequest request) {
        super(request);
    }
    
    @Override
    public ExcelImportResponse createResponse() {
        List<Financial> data = processExcelData();
        
        return ExcelImportResponse.builder()
            .fileName(request.getFile().getOriginalFilename())
            .mediaType(getMediaType())
            .fileSize(getFileSize())
            .data(data)
            .processedAt(LocalDateTime.now())
            .success(true)
            .build();
    }
    
    private List<Financial> processExcelData() {
        try (InputStream inputStream = request.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            return new FinancialTemplate(workbook)
                .read(Financial::fromRow);
                
        } catch (IOException e) {
            throw new FileProcessingException("Excel 처리 실패", e);
        }
    }
}
```

### 4. Template Method Pattern Excel 처리

#### 함수형 Excel 템플릿
```java
@FunctionalInterface
public interface ExcelImportTemplate<T, R> {
    List<R> read(Function<T, R> mapper);
}

public class FinancialTemplate implements ExcelImportTemplate<Row, Financial> {
    
    private final Workbook workbook;
    
    public FinancialTemplate(Workbook workbook) {
        this.workbook = workbook;
    }
    
    @Override
    public List<Financial> read(Function<Row, Financial> mapper) {
        Sheet sheet = workbook.getSheetAt(0);
        
        return StreamSupport.stream(sheet.spliterator(), false)
            .skip(1) // 헤더 행 제외
            .filter(row -> !isEmptyRow(row))
            .map(mapper)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    private boolean isEmptyRow(Row row) {
        return row.getPhysicalNumberOfCells() == 0 ||
               StreamSupport.stream(row.spliterator(), false)
                   .allMatch(cell -> cell.getCellType() == CellType.BLANK);
    }
}
```

#### Financial 도메인 모델
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Financial {
    
    private String segment;
    private String country;
    private String product;
    private String discountBand;
    private Double unitsSold;
    private BigDecimal manufacturingPrice;
    private BigDecimal salePrice;
    private BigDecimal grossSales;
    private BigDecimal sales;
    private BigDecimal cogs;
    private BigDecimal profit;
    private LocalDate date;
    
    public static Financial fromRow(Row row) {
        if (row.getPhysicalNumberOfCells() < 12) {
            return null; // 필수 컬럼 수 미달
        }
        
        return Financial.builder()
            .segment(getCellStringValue(row, 0))
            .country(getCellStringValue(row, 1))
            .product(getCellStringValue(row, 2))
            .discountBand(getCellStringValue(row, 3))
            .unitsSold(getCellNumericValue(row, 4))
            .manufacturingPrice(getCellBigDecimalValue(row, 5))
            .salePrice(getCellBigDecimalValue(row, 6))
            .grossSales(getCellBigDecimalValue(row, 7))
            .sales(getCellBigDecimalValue(row, 9))
            .cogs(getCellBigDecimalValue(row, 10))
            .profit(getCellBigDecimalValue(row, 11))
            .date(getCellDateValue(row, 12))
            .build();
    }
    
    private static String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        return cell != null ? cell.getStringCellValue() : "";
    }
    
    private static Double getCellNumericValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        return cell != null && cell.getCellType() == CellType.NUMERIC ? 
            cell.getNumericCellValue() : 0.0;
    }
    
    private static BigDecimal getCellBigDecimalValue(Row row, int cellIndex) {
        Double value = getCellNumericValue(row, cellIndex);
        return BigDecimal.valueOf(value);
    }
    
    private static LocalDate getCellDateValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell != null && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        return LocalDate.now();
    }
}
```

### 5. Spring Boot Actuator 모니터링

#### Actuator 설정
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: "*"
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
    metrics:
      enabled: true
    info:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
        method.timed: 0.5, 0.95, 0.99
  info:
    env:
      enabled: true
    build:
      enabled: true
    git:
      enabled: true
      mode: full

# 애플리케이션 정보
info:
  application:
    name: File Processing & Monitoring System
    description: AWS S3 integration with advanced file processing
    version: '@project.version@'
  features:
    - AWS S3 Integration
    - Excel/CSV Processing
    - File Validation Chain
    - Error Monitoring (Sentry)
```

#### 커스텀 헬스 인디케이터
```java
@Component
public class S3HealthIndicator implements HealthIndicator {
    
    private final S3Client s3Client;
    private final S3Properties s3Properties;
    
    @Override
    public Health health() {
        try {
            HeadBucketRequest request = HeadBucketRequest.builder()
                .bucket(s3Properties.getBucketName())
                .build();
                
            s3Client.headBucket(request);
            
            return Health.up()
                .withDetail("bucket", s3Properties.getBucketName())
                .withDetail("region", s3Properties.getRegion())
                .withDetail("status", "accessible")
                .build();
                
        } catch (NoSuchBucketException e) {
            return Health.down()
                .withDetail("bucket", s3Properties.getBucketName())
                .withDetail("error", "bucket not found")
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("bucket", s3Properties.getBucketName())
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### 6. Sentry 에러 모니터링 시스템

#### Sentry 자동 설정
```java
@Configuration
@ConditionalOnClass(Sentry.class)
@EnableConfigurationProperties(SentryProperties.class)
public class SentryAutoConfiguration {
    
    @Bean
    @ConditionalOnProperty(prefix = "sentry", name = "dsn")
    public SentryInitializer sentryInitializer(SentryProperties properties) {
        return new SentryInitializer(properties);
    }
    
    @Component
    @RequiredArgsConstructor
    public static class SentryInitializer {
        
        private final SentryProperties properties;
        
        @PostConstruct
        public void initialize() {
            Sentry.init(options -> {
                options.setDsn(properties.getDsn());
                options.setEnvironment(properties.getEnvironment());
                options.setServerName(properties.getServerName());
                options.setRelease(properties.getRelease());
                options.setTracesSampleRate(properties.getTracesSampleRate());
                options.setDebug(properties.isDebug());
            });
        }
    }
}

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({SentryAutoConfiguration.class})
public @interface EnableSentry {
}
```

#### 글로벌 예외 처리와 Sentry 연동
```java
@RestControllerAdvice
@Slf4j
public class PrimaveraRestExceptionHandler {
    
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorResponse> handleFileProcessing(FileProcessingException e) {
        log.error("파일 처리 오류", e);
        
        // Sentry에 에러 전송
        Sentry.captureException(e);
        
        ErrorResponse response = ErrorResponse.builder()
            .code("FILE_PROCESSING_ERROR")
            .message("파일 처리 중 오류가 발생했습니다")
            .details(e.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
            
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(response);
    }
    
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(StorageException e) {
        log.error("스토리지 오류", e);
        
        // 컨텍스트와 함께 Sentry에 에러 전송
        Sentry.withScope(scope -> {
            scope.setTag("storage.type", "s3");
            scope.setLevel(SentryLevel.ERROR);
            Sentry.captureException(e);
        });
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponse.builder()
                .code("STORAGE_ERROR")
                .message("파일 저장소 접근 오류")
                .timestamp(LocalDateTime.now())
                .build());
    }
}
```

## 🛠️ 기술 스택

### Core Framework
- **Spring Boot**: 3.3.6
- **Spring Web MVC**: REST API 및 파일 업로드
- **Spring Boot Actuator**: 애플리케이션 모니터링

### Cloud & Storage
- **AWS S3**: 클라우드 객체 스토리지
- **Spring Cloud AWS**: AWS 서비스 통합

### File Processing
- **Apache POI**: Excel 파일 읽기/쓰기 (4.1.2)
- **Apache Tika**: 파일 형식 감지 및 메타데이터 추출
- **OpenCSV**: CSV 파일 파싱
- **XLSX Streamer**: 대용량 Excel 스트리밍 처리

### Monitoring & Error Tracking
- **Sentry**: 실시간 에러 모니터링
- **Micrometer**: 메트릭 수집 및 모니터링

### Validation & Utility
- **Apache Commons IO**: 파일 I/O 유틸리티
- **Apache Commons Lang3**: 유틸리티 라이브러리

## 🚀 실행 방법

### 1. 인프라 환경 설정
```bash
# infrastructure 디렉터리로 이동
cd infrastructure

# 파일 처리용 Docker Compose 실행
docker-compose up -d

# 서비스 상태 확인
docker-compose ps
```

**포함된 서비스:**
- **MariaDB 11.4.7** (포트: 3308)
- **Sentry 24.1.0** (포트: 9000) - 에러 모니터링

### 2. AWS 설정 (옵션)

#### LocalStack으로 테스트
```bash
# LocalStack S3 실행
docker run --rm -d \
  -p 4566:4566 \
  -e SERVICES=s3 \
  localstack/localstack

# 테스트용 버킷 생성
aws --endpoint-url=http://localhost:4566 s3 mb s3://test-primavera-bucket
```

#### 실제 AWS S3 사용
```yaml
# application-local.yml
aws:
  credentials:
    access-key: YOUR_ACCESS_KEY
    secret-key: YOUR_SECRET_KEY
  region: ap-northeast-2
  s3:
    bucket-name: your-s3-bucket-name
```

### 3. 애플리케이션 실행
```bash
# 로컬 프로파일로 실행
./gradlew :chap16:bootRun -Dspring.profiles.active=local

# 또는 환경 변수 방식
SPRING_PROFILES_ACTIVE=local ./gradlew :chap16:bootRun
```

### 4. API 테스트
```bash
# Excel 파일 업로드
curl -X POST http://localhost:8080/excel/upload \
  -F "name=financial-data" \
  -F "file=@sample-financial-data.xlsx"

# S3에 직접 파일 업로드
curl -X POST http://localhost:8080/s3/upload \
  -F "file=@document.pdf"

# 헬스 체크
curl http://localhost:8080/actuator/health

# 메트릭 확인
curl http://localhost:8080/actuator/metrics

# 파일 처리 메트릭
curl http://localhost:8080/actuator/metrics/files.processed
```

## 🎓 핵심 고급 학습 포인트

### 1. Design Patterns 실무 적용
- **Chain of Responsibility**: 파일 검증 체인의 유연한 확장
- **Factory Pattern**: 파일 타입별 처리 전략 분리
- **Template Method**: Excel 처리 로직의 재사용성 극대화
- **Strategy Pattern**: 다양한 파일 형식에 대한 처리 전략

### 2. Cloud Native 개발
- **AWS S3 Integration**: 클라우드 스토리지 활용 패턴
- **Configuration Management**: 환경별 설정 외부화
- **Health Checks**: 외부 의존성 상태 모니터링
- **Circuit Breaker**: 외부 서비스 장애 대응

### 3. 모니터링과 관찰성
- **Actuator Endpoints**: 다양한 운영 메트릭 노출
- **Custom Health Indicators**: 비즈니스 로직 건강성 체크
- **Error Tracking**: Sentry를 통한 실시간 에러 추적
- **Distributed Tracing**: 요청 흐름 추적

### 4. 파일 처리 최적화
- **Streaming Processing**: 대용량 파일의 메모리 효율적 처리
- **Validation Pipeline**: 체계적인 파일 검증 프로세스
- **Error Recovery**: 파일 처리 실패 시 복구 전략
- **Batch Processing**: 다수 파일 동시 처리

### 5. 보안과 검증
- **File Type Detection**: Apache Tika를 통한 정확한 파일 형식 감지
- **Size Limitation**: 파일 크기 제한을 통한 DoS 공격 방지
- **Virus Scanning**: 업로드된 파일의 악성코드 검사
- **Access Control**: S3 버킷의 적절한 권한 관리

## 🧪 테스트 실행

### 단위 테스트
```bash
./gradlew :chap16:test
```

### 통합 테스트
```bash
./gradlew :chap16:test --tests="*IntegrationTest"
```

### S3 통합 테스트
```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("S3 파일 서비스 통합 테스트")
class S3FileServiceIntegrationTest {

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
        DockerImageName.parse("localstack/localstack:latest"))
        .withServices(LocalStackContainer.Service.S3);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.s3.endpoint", localstack::getEndpoint);
        registry.add("aws.credentials.access-key", localstack::getAccessKey);
        registry.add("aws.credentials.secret-key", localstack::getSecretKey);
        registry.add("aws.region", localstack::getRegion);
    }

    @Test
    @DisplayName("S3 파일 업로드 및 다운로드 테스트")
    void shouldUploadAndDownloadFile() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "test.txt", "text/plain", "테스트 파일 내용".getBytes());
        String key = "test/" + UUID.randomUUID().toString();

        // When: 업로드
        S3FileMetadata metadata = s3FileService.uploadFile(key, file);

        // Then: 업로드 검증
        assertThat(metadata.getKey()).isEqualTo(key);
        assertThat(metadata.getOriginalFilename()).isEqualTo("test.txt");

        // When: 다운로드
        Optional<InputStream> downloadResult = s3FileService.downloadFile(key);

        // Then: 다운로드 검증
        assertThat(downloadResult).isPresent();
        
        String content = new String(downloadResult.get().readAllBytes());
        assertThat(content).isEqualTo("테스트 파일 내용");
    }
}
```

### 파일 검증 체인 테스트
```java
@Test
@DisplayName("파일 검증 체인 테스트")
void shouldValidateFileChain() {
    // Given
    List<Validator> validators = List.of(
        new NullValidator(),
        new FileSizeValidator(),
        new MediaTypeValidation()
    );
    
    MockMultipartFile validFile = new MockMultipartFile(
        "test.xlsx",
        "test.xlsx",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        createValidExcelBytes()
    );
    
    ExcelImportRequest request = new ExcelImportRequest("test", validFile);

    // When
    boolean result = validators.stream()
        .allMatch(validator -> validator.validate(request));

    // Then
    assertThat(result).isTrue();
}
```

## 📚 학습 순서

1. **디자인 패턴 이해**
   - Chain of Responsibility 패턴 구현
   - Factory 패턴의 실무 적용
   - Template Method 활용

2. **AWS S3 연동**
   - AWS SDK 설정 및 연동
   - 파일 업로드/다운로드 구현
   - S3 보안 및 권한 관리

3. **파일 처리 시스템**
   - Apache POI Excel 처리
   - Apache Tika 파일 형식 감지
   - 대용량 파일 스트리밍

4. **모니터링 구축**
   - Actuator 메트릭 설정
   - 커스텀 헬스 인디케이터
   - Sentry 에러 추적

5. **성능 최적화**
   - 파일 처리 성능 튜닝
   - 메모리 사용량 최적화
   - 동시 처리 성능 향상

## 🔧 주요 고급 애너테이션

| 애너테이션 | 용도 | 사용 예시 |
|-----------|------|----------|
| `@EnableSentry` | Sentry 에러 추적 활성화 | 메인 애플리케이션 클래스 |
| `@ConditionalOnProperty` | 설정에 따른 Bean 생성 | Sentry 자동 설정 |
| `@ConfigurationProperties` | 외부 설정 바인딩 | AWS, Sentry 설정 |
| `@RestControllerAdvice` | 글로벌 예외 처리 | 에러 응답 표준화 |
| `@Component("healthIndicator")` | 커스텀 헬스 체크 | S3 상태 모니터링 |
| `@EventListener` | 파일 처리 이벤트 | 처리 완료 알림 |

## 📊 모니터링 대시보드

### Actuator 엔드포인트
- `GET /actuator/health` - 애플리케이션 및 S3 상태
- `GET /actuator/metrics` - 전체 메트릭 목록
- `GET /actuator/metrics/files.processed` - 처리된 파일 수
- `GET /actuator/metrics/s3.upload.duration` - S3 업로드 시간
- `GET /actuator/info` - 애플리케이션 정보

### Sentry 대시보드
- 실시간 에러 발생 현황
- 파일 처리 성능 모니터링
- 사용자 세션 추적
- 릴리즈별 에러 비교

## 🔄 다음 단계

**Chapter 17 (마이크로서비스 아키텍처)**로 진행하여 다음 내용을 학습하세요:

- Spring Cloud Gateway API 게이트웨이
- 서비스 디스커버리 (Eureka)
- 분산 설정 관리 (Config Server)
- 서킷 브레이커 패턴 (Resilience4j)
- 분산 추적 (Zipkin)

---

이 모듈을 통해 실무에서 자주 사용되는 파일 처리 시스템의 전체 라이프사이클을 경험하고, 클라우드 네이티브 애플리케이션의 모니터링과 에러 추적 체계를 구축할 수 있습니다.