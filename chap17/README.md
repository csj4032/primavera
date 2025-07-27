# Chapter 17 - 파일 처리 & 에러 모니터링 📊

## 📋 개요

chap17은 **파일 처리와 에러 모니터링**을 중심으로 한 고급 Spring Boot 애플리케이션입니다. Excel/CSV 파일 업로드 및 처리, 다양한 파일 포맷 검증, 그리고 Sentry를 통한 실시간 에러 트래킹 시스템을 통합적으로 학습할 수 있습니다.

## 🎯 학습 목표

- **🗂️ 대용량 파일 처리**: Apache POI, Tika를 활용한 다양한 파일 형식 지원
- **✅ 파일 검증 시스템**: 체인 오브 리스폰시빌리티 패턴 기반 검증 체계
- **📈 에러 모니터링**: Sentry 통합을 통한 실시간 에러 트래킹
- **📊 데이터 분석**: 한국어 형태소 분석 및 통계 처리
- **🏭 팩토리 패턴**: 파일 타입별 처리 로직 분리

## 🛠️ 핵심 기술 스택

### File Processing
- **Apache POI 4.1.1** - Excel 파일 읽기/쓰기
- **Apache Tika 1.18** - 파일 형식 감지 및 메타데이터 추출
- **OpenCSV 5.3** - CSV 파일 파싱
- **XLSX Streamer 2.1.0** - 대용량 Excel 스트리밍 처리

### Text & Language Processing  
- **Open Korean Text 2.3.1** - 한국어 형태소 분석
- **JavaTuples 1.2** - 데이터 페어링 및 그룹화

### Monitoring & Error Tracking
- **Sentry 1.7.30** - 실시간 에러 추적 및 모니터링
- **Spring Boot Actuator** - 애플리케이션 상태 모니터링

### Infrastructure
- **Undertow** - 고성능 논블로킹 서버
- **MariaDB 11.4.7** - 관계형 데이터베이스
- **Spring HATEOAS** - RESTful API 설계

## 📚 주요 학습 내용

### 1. 파일 업로드 및 검증 시스템

#### Chain of Responsibility 패턴 기반 검증

```java
@Service
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService {
    
    private final Map<String, List<Validator>> validatorGroup;
    
    @Override
    public ExcelImportResponse excelImport(ExcelImportRequest request) {
        // 검증 체인 실행
        boolean valid = validatorGroup.get("sizeAndTypeValidation")
            .stream()
            .allMatch(v -> v.validate(request));
            
        if (valid) {
            return new ExcelTypeFile(request).getExcelImportResponse();
        }
        return new UnknownFile(request).getExcelImportResponse();
    }
}
```

#### 다중 검증기 구현

```java
// 파일 크기 검증
@Slf4j
public class FileSizeValidator implements Validator {
    @Override
    public boolean validate(ExcelImportRequest request) {
        try {
            long size = IOUtils.toByteArray(request.getInputStream()).length;
            log.info("File Size: {}", size);
            return size > 0;
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
    }
}

// 미디어 타입 검증
public class MediaTypeValidation implements Validator {
    @Override
    public boolean validate(ExcelImportRequest request) {
        try {
            String mediaType = new Tika().detect(request.getInputStream());
            return ALLOWED_TYPES.contains(mediaType);
        } catch (IOException e) {
            return false;
        }
    }
}
```

### 2. Factory Pattern을 통한 파일 타입별 처리

#### Abstract Factory 구현

```java
public abstract class AbstractResponseFactory {
    protected ExcelImportRequest excelImportRequest;
    
    public AbstractResponseFactory(ExcelImportRequest request) {
        this.excelImportRequest = request;
    }
    
    public abstract ExcelImportResponse getExcelImportResponse();
}

// Excel 파일 전용 팩토리
public class ExcelTypeFile extends AbstractResponseFactory {
    public ExcelTypeFile(ExcelImportRequest request) {
        super(request);
    }
    
    @Override
    public ExcelImportResponse getExcelImportResponse() {
        return ExcelImportResponse.builder()
            .mediaType(getMediaType())
            .fileSize(getFileSize())
            .fileName(excelImportRequest.getName())
            .data(processExcelData())
            .build();
    }
}
```

### 3. Apache POI를 활용한 Excel 처리

#### Financial 데이터 모델 매핑

```java
@Getter
@Setter
@Builder
@ToString
public class Financial {
    private String segment;
    private String country;
    private String product;
    private String discountBand;
    private Double unitsSold;
    private BigDecimal manufacturingPrice;
    private BigDecimal salePrice;
    private BigDecimal grossSales;
    private String discounts;
    private BigDecimal sales;
    private BigDecimal cogs;
    private BigDecimal profit;
    private Instant date;
    
    // Row 데이터를 Financial 객체로 변환
    public static Financial of(Row row) {
        return Financial.builder()
            .segment(row.getCell(0).getStringCellValue())
            .country(row.getCell(1).getStringCellValue())
            .product(row.getCell(2).getStringCellValue())
            .discountBand(row.getCell(3).getStringCellValue())
            .unitsSold(row.getCell(4).getNumericCellValue())
            .manufacturingPrice(BigDecimal.valueOf(row.getCell(5).getNumericCellValue()))
            .salePrice(BigDecimal.valueOf(row.getCell(6).getNumericCellValue()))
            .grossSales(BigDecimal.valueOf(row.getCell(7).getNumericCellValue()))
            .discounts(row.getCell(8).getStringCellValue())
            .sales(BigDecimal.valueOf(row.getCell(9).getNumericCellValue()))
            .cogs(BigDecimal.valueOf(row.getCell(10).getNumericCellValue()))
            .profit(BigDecimal.valueOf(row.getCell(11).getNumericCellValue()))
            .date(row.getCell(12).getDateCellValue().toInstant())
            .build();
    }
}
```

#### 함수형 프로그래밍 기반 Excel 템플릿

```java
@FunctionalInterface
public interface ExcelImportTemplate<T, R> {
    List<R> read(final Function<T, R> function);
}

// 사용 예시
public class FinancialTemplate implements ExcelImportTemplate<Row, Financial> {
    
    private final Workbook workbook;
    
    @Override
    public List<Financial> read(final Function<Row, Financial> function) {
        return StreamSupport.stream(workbook.getSheetAt(0).spliterator(), false)
            .skip(1) // 헤더 행 제외
            .map(function)
            .collect(Collectors.toList());
    }
}
```

### 4. CSV 파일 처리 및 한국어 분석

#### KakaoTalk 채팅 데이터 모델

```java
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KakaoTalkChat {
    
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    @CsvBindByPosition(position = 0, required = true)
    @CsvBindByName
    private LocalDateTime date;
    
    @CsvBindByPosition(position = 1, required = true)
    @CsvBindByName
    private String user;
    
    @CsvBindByPosition(position = 2, required = true)
    @CsvBindByName
    private String message;
}
```

#### 한국어 형태소 분석 및 통계 처리

```java
@Test
public void kakaoRepository() throws IOException {
    // CSV 파일 파싱
    var kakaoTalkChats = kakaoTalkChatRepository
        .getKakaoTalkChatByName("kakaoTalk/chat/kakaoTalk_Chat.csv");
    
    // 사용자별 메시지 수 통계 (상위 10명)
    Map<String, Long> countMessageByUserOrder = kakaoTalkChats.stream()
        .collect(Collectors.groupingBy(KakaoTalkChat::getUser, Collectors.counting()))
        .entrySet().stream()
        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .limit(10)
        .collect(Collectors.toMap(
            Map.Entry::getKey, 
            Map.Entry::getValue, 
            (e1, e2) -> e1, 
            LinkedHashMap::new
        ));
    
    // 한국어 형태소 분석을 통한 단어 빈도 분석
    Map<String, Long> wordCount = kakaoTalkChats.parallelStream()
        .map(e -> tokensToJavaKoreanTokenList(tokenize(normalize(e.getMessage()))))
        .flatMap(e -> e.stream())
        .filter(e -> e.getPos().equals(Noun)) // 명사만 추출
        .collect(Collectors.groupingBy(KoreanTokenJava::getText, Collectors.counting()))
        .entrySet().stream()
        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
        .limit(10)
        .collect(Collectors.toMap(
            Map.Entry::getKey, 
            Map.Entry::getValue, 
            (e1, e2) -> e1, 
            LinkedHashMap::new
        ));
    
    log.info("Top 10 words: {}", wordCount);
}
```

### 5. Sentry 에러 모니터링 시스템

#### 커스텀 Sentry Auto Configuration

```java
@Configuration
@ConditionalOnClass(Sentry.class)
@EnableConfigurationProperties(SentryProperties.class)
public class SentryAutoConfiguration {
    
    @Bean
    public SentryClient sentry(SentryProperties properties) {
        SentryClient sentryClient = Sentry.init(properties.getDns());
        sentryClient.setEnvironment(properties.getEnvironment());
        sentryClient.setServerName(properties.getServername());
        sentryClient.setRelease(properties.getRelease());
        return sentryClient;
    }
}
```

#### Enable 어노테이션을 통한 간편 설정

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({SentryAutoConfiguration.class})
public @interface EnableSentry {
}

// 메인 애플리케이션에서 사용
@Slf4j
@EnableSentry  // 한 줄로 Sentry 모니터링 활성화
@SpringBootApplication
public class FileProcessingMonitoringApplication {
    // ...
}
```

#### Sentry 설정 프로퍼티

```yaml
sentry:
  dns: https://4084f8500752461897ebbfe3a067d36c@sentry.io/5166811
  environment: production
  servername: chap17
  release: 0.0.1-SNAPSHOT
```

### 6. RESTful API 및 HATEOAS 구현

#### 파일 업로드 컨트롤러

```java
@Slf4j
@RestController
@RequiredArgsConstructor
public class ExcelImportController {
    
    private final ExcelImportService excelImportService;
    private final Validator validator;
    
    @PostMapping(value = "/save", produces = "application/hal+json")
    public ResponseEntity<ExcelImportResponse> save(ExcelImportRequest request) 
            throws IOException {
        
        log.info("Validator: {}", validator.toString());
        ExcelImportResponse response = excelImportService.excelImport(request);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
```

#### HATEOAS 응답 형식

```java
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExcelImportResponse {
    private String fileName;
    private String mediaType;
    private long fileSize;
    private List<Financial> data;
    private LocalDateTime processedAt;
    
    // HATEOAS 링크 정보 포함
    private List<Link> links;
}
```

### 7. 고급 파일 처리 패턴

#### Multipart File 인터페이스 구현

```java
public class ExcelImportRequest implements ExcelFileValid {
    
    private String name;
    private MultipartFile file;
    
    public long getSize() {
        if (Objects.isNull(file)) return 0;
        return file.getSize();
    }
    
    @Override
    public InputStream getInputStream() {
        try {
            return this.getFile().getInputStream();
        } catch (IOException | NullPointerException e) {
            log.error(e.getMessage());
            return EmptyInputStream.INSTANCE;
        }
    }
}
```

#### 파일 크기 제한 설정

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 128KB    # 개별 파일 최대 크기
      max-request-size: 128KB  # 전체 요청 최대 크기
```

## 🔧 실습 예제

### 파일 업로드 테스트

```bash
# Excel 파일 업로드 테스트
curl -X POST http://localhost:8080/save \
  -H "Content-Type: multipart/form-data" \
  -F "name=financial-data" \
  -F "file=@20191225.xlsx"

# CSV 파일 업로드 테스트  
curl -X POST http://localhost:8080/save \
  -H "Content-Type: multipart/form-data" \
  -F "name=kakao-chat" \
  -F "file=@kakaoTalk_Chat.csv"
```

### 응답 예시

```json
{
  "fileName": "financial-data",
  "mediaType": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  "fileSize": 65432,
  "processedAt": "2024-01-20T15:30:45",
  "data": [
    {
      "segment": "Government",
      "country": "Canada", 
      "product": "Carretera",
      "discountBand": "None",
      "unitsSold": 1618.5,
      "manufacturingPrice": 3.0,
      "salePrice": 20.0,
      "grossSales": 32370.0,
      "sales": 32370.0,
      "cogs": 16185.0,
      "profit": 16185.0,
      "date": "2014-01-01T00:00:00Z"
    }
  ],
  "_links": {
    "self": {
      "href": "http://localhost:8080/save"
    },
    "download": {
      "href": "http://localhost:8080/download/financial-data"
    }
  }
}
```

## 🧪 테스트 전략

### 통합 테스트

```java
@SpringBootTest
@ActiveProfiles("test")
class ExcelImportControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("Excel 파일 업로드 및 처리 통합 테스트")
    void shouldProcessExcelFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            getClass().getResourceAsStream("/20191225.xlsx")
        );
        
        mockMvc.perform(multipart("/save")
                .file(file)
                .param("name", "test-financial-data"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("test-financial-data"))
                .andExpect(jsonPath("$.mediaType").exists())
                .andExpect(jsonPath("$.fileSize").exists())
                .andExpect(jsonPath("$.data").isArray());
    }
}
```

### 파일 검증 테스트

```java
@Test
@DisplayName("파일 크기 검증 테스트")
void shouldValidateFileSize() {
    FileSizeValidator validator = new FileSizeValidator();
    ExcelImportRequest request = createTestRequest();
    
    boolean result = validator.validate(request);
    
    assertTrue(result);
}
```

## 📊 성능 최적화

### 대용량 파일 스트리밍 처리

```java
// XLSX Streamer를 사용한 메모리 효율적인 처리
public List<Financial> processLargeExcelFile(InputStream inputStream) {
    try (Workbook workbook = StreamingReader.builder()
            .rowCacheSize(100)    // 메모리에 캐시할 행 수
            .bufferSize(4096)     // 버퍼 크기
            .open(inputStream)) {
            
        return StreamSupport.stream(workbook.getSheetAt(0).spliterator(), false)
            .skip(1) // 헤더 제외
            .map(Financial::of)
            .collect(Collectors.toList());
    }
}
```

### 병렬 처리를 통한 성능 향상

```java
// 병렬 스트림을 활용한 한국어 형태소 분석
Map<String, Long> wordCount = kakaoTalkChats.parallelStream()
    .map(this::extractNouns)
    .flatMap(Collection::stream)
    .collect(Collectors.groupingBy(
        Function.identity(), 
        Collectors.counting()
    ));
```

## 🚀 실행 방법

### 환경 설정

1. **MariaDB 데이터베이스 시작**
```bash
# Infrastructure Docker 환경 시작
cd infrastructure
docker-compose up -d
```

2. **애플리케이션 실행**
```bash
./gradlew :chap17:bootRun
```

3. **테스트 실행**
```bash
./gradlew :chap17:test
```

### API 엔드포인트

| 엔드포인트 | 메서드 | 설명 | Content-Type |
|-----------|--------|------|--------------|
| `/save` | POST | 파일 업로드 및 처리 | multipart/form-data |
| `/actuator/health` | GET | 헬스 체크 | application/json |
| `/actuator/info` | GET | 애플리케이션 정보 | application/json |

## 📈 모니터링 및 로깅

### Sentry 대시보드 활용

- **실시간 에러 추적**: 파일 처리 중 발생하는 예외 상황 모니터링
- **성능 메트릭**: 파일 처리 시간 및 메모리 사용량 추적
- **사용자 세션**: 파일 업로드 패턴 분석

### 구조화된 로깅

```xml
<!-- logback.xml 설정 -->
<configuration>
    <appender name="SENTRY" class="io.sentry.logback.SentryAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>WARN</level>
        </filter>
    </appender>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="SENTRY"/>
    </root>
</configuration>
```

## 📖 참고 자료

### 공식 문서
- [Apache POI Documentation](https://poi.apache.org/components/spreadsheet/)
- [Apache Tika Documentation](https://tika.apache.org/1.18/gettingstarted.html)
- [Sentry Java Documentation](https://docs.sentry.io/platforms/java/)
- [Spring HATEOAS Reference](https://docs.spring.io/spring-hateoas/docs/current/reference/html/)

### 라이브러리 가이드
- [OpenCSV User Guide](http://opencsv.sourceforge.net/)
- [Open Korean Text Processing](https://github.com/twitter/twitter-korean-text)
- [XLSX Streamer](https://github.com/monitorjbl/excel-streaming-reader)

## 🚀 다음 단계

다음 Chapter에서는 **CI/CD 파이프라인 구축**을 학습합니다:
- GitHub Actions를 통한 자동화된 빌드
- Docker 컨테이너 배포
- Kubernetes 오케스트레이션
- 모니터링 및 알럿 시스템 구축

---

**🎓 학습 포인트**: 파일 처리는 엔터프라이즈 애플리케이션의 핵심 기능입니다. 검증, 변환, 모니터링을 체계적으로 구현하면 안정적이고 확장 가능한 시스템을 구축할 수 있습니다.