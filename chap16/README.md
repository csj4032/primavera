# Chapter 16 - AWS S3 + Vault + JOOQ File Processing System 🔄

## 📋 개요

chap16은 **AWS S3 클라우드 스토리지와 JOOQ를 활용한 파일 처리 시스템**을 다루는 실무 중심 프로젝트입니다. 로컬 파일 업로드부터 S3 클라우드 저장, 데이터 가공 처리, JOOQ 기반 데이터베이스 저장까지 - 현대 클라우드 환경에서의 완전한 파일 처리 워크플로우를 학습할 수 있습니다.

## 🎯 학습 목표

### 3가지 파일 처리 시나리오 마스터
1. **📤 로컬 파일 업로드 → 가공 → 데이터베이스 저장**: 전통적인 파일 처리 워크플로우
2. **☁️ 로컬 파일 업로드 → AWS S3 업로드**: 클라우드 스토리지 활용한 파일 백업
3. **📥 S3 다운로드 → 가공 → JOOQ 데이터베이스 저장**: 클라우드 기반 데이터 파이프라인

### 핵심 기술 습득
- **☁️ AWS S3**: 클라우드 객체 스토리지 서비스 통합
- **🔧 JOOQ**: 타입 안전 SQL 쿼리 빌더 및 코드 생성
- **📊 데이터 가공**: TableSaw, jOOλ을 활용한 함수형 데이터 처리
- **🔄 워크플로우 엔진**: Easy Flows를 통한 파일 처리 파이프라인 구축
- **📈 모니터링**: Sentry 통합을 통한 실시간 에러 트래킹

## 🛠️ 핵심 기술 스택

### DataFrame & Data Manipulation Libraries
- **TableSaw 0.43.1** - Java의 Pandas 대안, DataFrame 조작 및 시각화
- **Weka 3.8.6** - 머신러닝 및 데이터 마이닝 툴킷
- **Smile 3.1.1** - 고성능 머신러닝 라이브러리
- **Apache Commons Math 3.6.1** - 수학 연산 및 통계 처리

### Functional Programming & Collections
- **jOOλ(jool) 0.9.15** - SQL 스타일 함수형 프로그래밍 유틸리티
- **Vavr 0.10.4** - 함수형 프로그래밍 라이브러리 (불변 컬렉션, 패턴 매칭)
- **Eclipse Collections 11.1.0** - 고성능 컬렉션 프레임워크
- **JavaTuples 1.2** - 타입 안전 튜플 구현

### Stream Processing & Data Flow
- **Easy Flows 0.3.0** - 워크플로우 엔진 및 데이터 파이프라인
- **StreamWork 1.0.0** - 스트림 기반 데이터 처리

### File Processing
- **Apache POI 4.1.1** - Excel 파일 읽기/쓰기
- **Apache Tika 1.18** - 파일 형식 감지 및 메타데이터 추출
- **OpenCSV 5.3** - CSV 파일 파싱
- **XLSX Streamer 2.1.0** - 대용량 Excel 스트리밍 처리

### Text & Language Processing  
- **Open Korean Text 2.3.1** - 한국어 형태소 분석

### AWS & Cloud Integration
- **Spring Cloud AWS** - AWS 서비스 통합
- **AWS S3** - 클라우드 객체 스토리지
- **Spring Cloud Vault** - 보안 자격증명 관리

### Monitoring & Error Tracking
- **Sentry Cloud** - 실시간 에러 추적 및 모니터링 (클라우드 Developer 플랜)
- **Spring Boot Actuator** - 애플리케이션 상태 모니터링

### Infrastructure
- **Undertow** - 고성능 논블로킹 서버
- **MariaDB 11.4.7** - 관계형 데이터베이스
- **Spring HATEOAS** - RESTful API 설계

## 📚 주요 학습 내용

### 1. TableSaw DataFrame 조작 (Pandas 스타일)

#### DataFrame 생성 및 기본 조작

```java
@Test
@DisplayName("TableSaw DataFrame 기본 조작")
void tablesawBasicOperations() {
    // CSV에서 DataFrame 생성
    Table financialData = Table.read().csv("financial_data.csv");
    
    // 기본 정보 확인
    System.out.println("Shape: " + financialData.shape());
    System.out.println("Column Names: " + financialData.columnNames());
    System.out.println("Structure:\n" + financialData.structure());
    
    // 데이터 필터링 (Pandas의 df[df['column'] > value] 스타일)
    Table highProfitData = financialData.where(
        financialData.numberColumn("Profit").isGreaterThan(10000)
    );
    
    // 그룹화 및 집계 (Pandas의 groupby 스타일)
    Table countryStats = financialData
        .summarize("Sales", AggregateFunctions.sum, AggregateFunctions.mean)
        .by("Country");
    
    System.out.println("Country Statistics:\n" + countryStats);
    
    // 새로운 컬럼 추가 (계산된 컬럼)
    financialData.addColumns(
        financialData.numberColumn("Sales")
            .subtract(financialData.numberColumn("COGS"))
            .setName("Gross Profit")
    );
}
```

#### 고급 DataFrame 연산

```java
@Test
@DisplayName("TableSaw 고급 DataFrame 연산")
void tablesawAdvancedOperations() {
    Table sales = Table.read().csv("sales.csv");
    
    // 피벗 테이블 생성 (Pandas의 pivot_table 스타일)
    Table pivotTable = sales.xTabCounts("Country", "Product");
    
    // 윈도우 함수 (이동 평균 계산)
    DoubleColumn salesColumn = sales.numberColumn("Sales");
    DoubleColumn movingAverage = salesColumn.rolling(3).mean().setName("Moving_Avg_3");
    sales.addColumns(movingAverage);
    
    // 상위/하위 N개 레코드
    Table top10 = sales.sortDescendingOn("Sales").first(10);
    
    // 데이터 타입 변환 및 결측치 처리
    sales.replaceColumn("Date", 
        sales.dateColumn("Date").map(LocalDate::toString));
    
    // 조건부 컬럼 생성 (Pandas의 np.where 스타일)
    StringColumn salesCategory = sales.numberColumn("Sales").map(
        value -> value > 50000 ? "High" : value > 20000 ? "Medium" : "Low"
    ).setName("Sales_Category");
    
    sales.addColumns(salesCategory);
}
```

### 2. jOOλ(jool) 함수형 프로그래밍

#### SQL 스타일 데이터 조작

```java
@Test
@DisplayName("jOOλ SQL 스타일 데이터 처리")
void joolSqlStyleProcessing() {
    List<Financial> financials = loadFinancialData();
    
    // SQL SELECT 스타일 - 컬럼 선택 및 변환
    List<Tuple2<String, Double>> countryTotalSales = Seq.seq(financials)
        .groupBy(Financial::getCountry)
        .map(group -> tuple(
            group.v1, 
            group.v2.sumDouble(Financial::getSales)
        ))
        .sorted(Comparator.comparing(Tuple2::v2, reverseOrder()))
        .toList();
    
    // SQL WHERE 스타일 - 복합 조건 필터링
    List<Financial> filteredData = Seq.seq(financials)
        .filter(f -> f.getSales() > 10000)
        .filter(f -> f.getCountry().equals("Canada"))
        .filter(f -> f.getProduct().startsWith("Car"))
        .toList();
    
    // SQL HAVING 스타일 - 그룹화 후 조건 적용
    Map<String, Double> productAvgProfit = Seq.seq(financials)
        .groupBy(Financial::getProduct)
        .filter(group -> group.v2.size() >= 5) // HAVING COUNT(*) >= 5
        .toMap(
            group -> group.v1,
            group -> group.v2.averageDouble(Financial::getProfit)
        );
}
```

#### 함수형 파이프라인 구축

```java
@Test
@DisplayName("jOOλ 함수형 파이프라인")
void joolFunctionalPipeline() {
    List<Financial> data = loadFinancialData();
    
    // 복잡한 데이터 변환 파이프라인
    Map<String, List<Tuple3<String, Double, String>>> 
        countryProductAnalysis = Seq.seq(data)
        
        // 1단계: 수익성 있는 제품만 필터링
        .filter(f -> f.getProfit() > 0)
        
        // 2단계: 국가별 그룹화
        .groupBy(Financial::getCountry)
        
        // 3단계: 각 국가의 상위 제품 분석
        .toMap(
            entry -> entry.v1, // 국가명
            entry -> Seq.seq(entry.v2)
                .groupBy(Financial::getProduct)
                .map(productGroup -> tuple(
                    productGroup.v1, // 제품명
                    productGroup.v2.sumDouble(Financial::getProfit), // 총 수익
                    productGroup.v2.maxBy(Financial::getProfit)
                        .map(Financial::getDiscountBand)
                        .orElse("Unknown") // 최고 수익 할인 밴드
                ))
                .sorted(Comparator.comparing(Tuple3::v2, reverseOrder()))
                .limit(3) // 상위 3개 제품
                .toList()
        );
}
```

### 3. Vavr 함수형 프로그래밍

#### 불변 컬렉션 및 패턴 매칭

```java
@Test
@DisplayName("Vavr 불변 컬렉션 및 패턴 매칭")
void vavrImmutableCollections() {
    // 불변 List 생성 및 조작
    List<Financial> immutableList = List.ofAll(loadFinancialData());
    
    // 함수형 변환 체인
    List<String> topCountries = immutableList
        .groupBy(Financial::getCountry)
        .mapValues(countryData -> countryData.sumBy(Financial::getSales))
        .toList()
        .sortBy(Tuple2::_2, Comparator.reverseOrder())
        .take(5)
        .map(Tuple2::_1);
    
    // Try 모나드를 활용한 안전한 처리
    String result = immutableList.headOption()
        .map(financial -> Try.of(() -> 
            financial.getSales() / financial.getCogs()
        ))
        .getOrElse(Try.success(0.0))
        .map(ratio -> String.format("Profit Margin: %.2f%%", ratio * 100))
        .getOrElse("계산 실패");
    
    // Option 모나드 체인
    Option<Financial> bestPerformer = immutableList
        .filter(f -> f.getCountry().equals("USA"))
        .maxBy(Financial::getProfit);
    
    String analysis = bestPerformer
        .map(f -> f.getProduct() + " - $" + f.getProfit())
        .getOrElse("데이터 없음");
}
```

#### Either 모나드를 활용한 에러 처리

```java
@Test
@DisplayName("Vavr Either 모나드 에러 처리")
void vavrEitherErrorHandling() {
    
    // Either를 반환하는 안전한 계산 함수
    Function<Financial, Either<String, Double>> calculateMargin = financial ->
        financial.getSales() == 0 
            ? Either.left("매출이 0입니다")
            : Either.right((financial.getSales() - financial.getCogs()) / financial.getSales());
    
    List<Financial> data = List.ofAll(loadFinancialData());
    
    // Either 체인을 통한 안전한 데이터 처리
    List<Either<String, Tuple2<String, Double>>> results = data
        .map(financial -> calculateMargin.apply(financial)
            .map(margin -> Tuple.of(financial.getProduct(), margin))
        );
    
    // 성공과 실패 분리 처리
    List<Tuple2<String, Double>> successful = results
        .filter(Either::isRight)
        .map(Either::get);
        
    List<String> errors = results
        .filter(Either::isLeft)
        .map(Either::getLeft);
    
    log.info("성공한 계산: {}", successful.size());
    log.info("실패한 계산: {}", errors);
}
```

### 4. Eclipse Collections 고성능 컬렉션

#### 메모리 효율적인 Primitive Collections

```java
@Test
@DisplayName("Eclipse Collections Primitive Collections")
void eclipseCollectionsPrimitives() {
    List<Financial> data = loadFinancialData();
    
    // Primitive 컬렉션으로 메모리 효율성 극대화
    DoubleList salesData = new DoubleArrayList();
    IntList monthData = new IntArrayList();
    
    data.forEach(financial -> {
        salesData.add(financial.getSales());
        monthData.add(financial.getDate().getMonthValue());
    });
    
    // 고성능 통계 계산
    double totalSales = salesData.sum();
    double avgSales = salesData.average();
    double maxSales = salesData.max();
    double minSales = salesData.min();
    
    // 월별 매출 그룹화 (primitive 기반)
    IntObjectMap<DoubleList> salesByMonth = new IntObjectHashMap<>();
    
    for (int i = 0; i < monthData.size(); i++) {
        int month = monthData.get(i);
        double sales = salesData.get(i);
        
        salesByMonth.getIfAbsentPut(month, DoubleArrayList::new)
                   .add(sales);
    }
    
    // 월별 통계 계산
    IntObjectMap<DoubleSummaryStatistics> monthlyStats = salesByMonth
        .collectValues((month, monthSales) -> new DoubleSummaryStatistics(
            monthSales.sum(),
            monthSales.average(),
            monthSales.max(),
            monthSales.min(),
            monthSales.size()
        ));
}
```

#### 고급 컬렉션 연산

```java
@Test
@DisplayName("Eclipse Collections 고급 연산")
void eclipseCollectionsAdvanced() {
    MutableList<Financial> data = Lists.mutable.ofAll(loadFinancialData());
    
    // Partition (조건에 따른 분할)
    PartitionMutableList<Financial> partitioned = 
        data.partition(f -> f.getProfit() > 5000);
    
    MutableList<Financial> profitable = partitioned.getSelected();
    MutableList<Financial> unprofitable = partitioned.getRejected();
    
    // GroupBy with aggregation
    Multimap<String, Financial> byCountry = data.groupBy(Financial::getCountry);
    
    MutableMap<String, Double> countryTotals = byCountry.keyMultiValuePairsView()
        .toMap(
            Pair::getOne,
            pair -> pair.getTwo().sumOfDouble(Financial::getSales)
        );
    
    // Zip 연산 (두 컬렉션 결합)
    MutableList<String> products = data.collect(Financial::getProduct);
    MutableList<Double> profits = data.collect(Financial::getProfit);
    
    MutableList<Pair<String, Double>> productProfitPairs = 
        products.zip(profits);
    
    // Cartesian Product (직교곱)
    MutableList<String> categories = Lists.mutable.of("Electronics", "Clothing", "Food");
    MutableList<String> regions = Lists.mutable.of("North", "South", "East", "West");
    
    MutableList<Pair<String, String>> categoryRegionCombos = 
        categories.flatCollect(cat -> 
            regions.collect(region -> Tuples.pair(cat, region))
        );
}
```

### 5. Weka 머신러닝 통합

#### 데이터 전처리 및 모델 학습

```java
@Test
@DisplayName("Weka 머신러닝 파이프라인")
void wekaMachineLearningPipeline() throws Exception {
    // 1. 데이터 로딩 및 Weka Instances 변환
    List<Financial> financialData = loadFinancialData();
    Instances dataset = convertToWekaInstances(financialData);
    
    // 2. 데이터 전처리
    // 결측치 제거
    RemoveMissingValues removeMissing = new RemoveMissingValues();
    removeMissing.setInputFormat(dataset);
    dataset = Filter.useFilter(dataset, removeMissing);
    
    // 수치형 속성 정규화
    Normalize normalize = new Normalize();
    normalize.setInputFormat(dataset);
    dataset = Filter.useFilter(dataset, normalize);
    
    // 3. 훈련/테스트 분할
    dataset.randomize(new Random(42));
    int trainSize = (int) Math.round(dataset.numInstances() * 0.8);
    int testSize = dataset.numInstances() - trainSize;
    
    Instances trainSet = new Instances(dataset, 0, trainSize);
    Instances testSet = new Instances(dataset, trainSize, testSize);
    
    // 4. 분류 모델 훈련 (Random Forest)
    RandomForest classifier = new RandomForest();
    classifier.setNumIterations(100);
    classifier.buildClassifier(trainSet);
    
    // 5. 모델 평가
    Evaluation eval = new Evaluation(trainSet);
    eval.evaluateModel(classifier, testSet);
    
    log.info("정확도: {}", eval.pctCorrect());
    log.info("혼동 행렬:\n{}", eval.toMatrixString());
    log.info("분류 리포트:\n{}", eval.toClassDetailsString());
}

private Instances convertToWekaInstances(List<Financial> data) {
    // Weka Instances 생성 로직
    ArrayList<Attribute> attributes = new ArrayList<>();
    attributes.add(new Attribute("sales"));
    attributes.add(new Attribute("cogs"));
    attributes.add(new Attribute("profit"));
    
    // 범주형 타겟 변수 (High/Medium/Low profit)
    ArrayList<String> profitCategories = new ArrayList<>();
    profitCategories.add("Low");
    profitCategories.add("Medium"); 
    profitCategories.add("High");
    attributes.add(new Attribute("profit_category", profitCategories));
    
    Instances instances = new Instances("financial_data", attributes, data.size());
    instances.setClassIndex(instances.numAttributes() - 1);
    
    // 데이터 인스턴스 추가
    for (Financial financial : data) {
        double[] values = new double[]{
            financial.getSales(),
            financial.getCogs(),
            financial.getProfit(),
            determineProfitCategory(financial.getProfit())
        };
        instances.add(new DenseInstance(1.0, values));
    }
    
    return instances;
}
```

### 6. Smile 고성능 머신러닝

#### 고급 회귀 분석

```java
@Test
@DisplayName("Smile 회귀 분석 및 예측")
void smileRegressionAnalysis() {
    List<Financial> data = loadFinancialData();
    
    // 특성 행렬 준비
    double[][] features = data.stream()
        .map(f -> new double[]{
            f.getUnitsSold(),
            f.getManufacturingPrice(),
            f.getSalePrice(),
            encodeCategory(f.getDiscountBand())
        })
        .toArray(double[][]::new);
    
    // 타겟 변수 (매출)
    double[] targets = data.stream()
        .mapToDouble(Financial::getSales)
        .toArray();
    
    // 훈련/테스트 분할
    int trainSize = (int) (features.length * 0.8);
    double[][] trainX = Arrays.copyOfRange(features, 0, trainSize);
    double[] trainY = Arrays.copyOfRange(targets, 0, trainSize);
    double[][] testX = Arrays.copyOfRange(features, trainSize, features.length);
    double[] testY = Arrays.copyOfRange(targets, trainSize, targets.length);
    
    // Random Forest 회귀 모델
    RandomForest model = RandomForest.fit(
        Formula.lhs("sales"), 
        DataFrame.of(trainX, "units", "mfg_price", "sale_price", "discount"),
        trainY
    );
    
    // 예측 수행
    double[] predictions = model.predict(
        DataFrame.of(testX, "units", "mfg_price", "sale_price", "discount")
    );
    
    // 모델 성능 평가
    double rmse = RMSE.of(testY, predictions);
    double mae = MAE.of(testY, predictions);
    double r2 = cor(testY, predictions);
    
    log.info("RMSE: {}", rmse);
    log.info("MAE: {}", mae);
    log.info("R²: {}", r2 * r2);
    
    // 특성 중요도 분석
    double[] importance = model.importance();
    String[] featureNames = {"Units Sold", "Mfg Price", "Sale Price", "Discount"};
    
    for (int i = 0; i < importance.length; i++) {
        log.info("특성 중요도 - {}: {}", featureNames[i], importance[i]);
    }
}
```

### 7. 파일 업로드 및 검증 시스템

#### Chain of Responsibility 패턴 기반 검증

chap16에서는 **Chain of Responsibility 패턴**을 활용하여 파일 업로드 시 다단계 검증을 수행합니다. 이 패턴을 통해 각 검증 단계를 독립적으로 관리하고 유연하게 확장할 수 있습니다.

```java
@Service
@RequiredArgsConstructor
public class ExcelImportServiceImpl implements ExcelImportService {
    
    private final Map<String, List<Validator>> validatorGroup;
    
    @Override
    public ExcelImportResponse excelImport(ExcelImportRequest request) {
        // 검증 체인 실행 - 모든 검증기가 통과해야 성공
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

#### 검증기 인터페이스 및 구현체

##### 1. 공통 Validator 인터페이스

```java
public interface Validator {
    boolean validate(ExcelImportRequest excelImportRequest);
}
```

##### 2. 파일 크기 검증기

```java
@Slf4j
public class FileSizeValidator implements Validator {
    @Override
    public boolean validate(ExcelImportRequest excelImportRequest) {
        log.info("File Size Validator");
        try {
            long size = IOUtils.toByteArray(excelImportRequest.getInputStream()).length;
            log.info("File Size : {}", size);
            return size > 0; // 파일 크기가 0보다 큰지 검증
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
    }
}
```

##### 3. 미디어 타입 검증기

```java
@Slf4j
public class MediaTypeValidation implements Validator {
    
    private static final CharSequence APPLICATION_X_TIKA_OOXML = "application/x-tika-ooxml";
    
    @Override
    public boolean validate(ExcelImportRequest excelImportRequest) {
        log.info("Media Type Validator");
        Tika tika = new Tika();
        try {
            String mediaType = tika.detect(excelImportRequest.getInputStream());
            log.info("mediaType : {}", mediaType);
            return mediaType.contains(APPLICATION_X_TIKA_OOXML); // Excel 파일 형식 검증
        } catch (IOException e) {
            log.error(e.getMessage());
            return false;
        }
    }
}
```

##### 4. Null 검증기

```java
@Slf4j
public class NullValidator implements Validator {
    @Override
    public boolean validate(ExcelImportRequest excelImportRequest) {
        log.info("Null Validation");
        return excelImportRequest != null; // 요청 객체가 null이 아닌지 검증
    }
}
```

##### 5. 버전 검증기

```java
@Slf4j
public class VersionValidation implements Validator {
    @Override
    public boolean validate(ExcelImportRequest excelImportRequest) {
        log.info("version validator");
        return false; // 현재는 항상 실패 (구현 예정)
    }
}
```

#### 검증 체인 구성 및 확장성

##### 검증기 그룹 설정

```java
@Configuration
public class ValidatorConfiguration {
    
    @Bean
    public Map<String, List<Validator>> validatorGroup() {
        Map<String, List<Validator>> groups = new HashMap<>();
        
        // 파일 크기 및 타입 검증 그룹
        groups.put("sizeAndTypeValidation", Arrays.asList(
            new NullValidator(),
            new FileSizeValidator(),
            new MediaTypeValidation()
        ));
        
        // 추가 검증 그룹 예시
        groups.put("securityValidation", Arrays.asList(
            new VirusScanner(),
            new MaliciousContentDetector()
        ));
        
        return groups;
    }
}
```

##### 검증 실패 처리

```java
// 검증 실패 시 UnknownFile 팩토리로 처리
public class UnknownFile extends AbstractResponseFactory {
    public UnknownFile(ExcelImportRequest request) {
        super(request);
    }
    
    @Override
    public ExcelImportResponse getExcelImportResponse() {
        return ExcelImportResponse.builder()
            .fileName(excelImportRequest.getName())
            .mediaType("unknown")
            .fileSize(0L)
            .data(Collections.emptyList())
            .error("파일 검증에 실패했습니다")
            .build();
    }
}
```

#### 검증 시스템의 장점

1. **확장 가능성**: 새로운 검증기를 쉽게 추가할 수 있음
2. **독립성**: 각 검증기는 독립적으로 작동하며 테스트 가능
3. **재사용성**: 검증기를 다른 검증 그룹에서 재사용 가능
4. **유연성**: 검증 그룹을 동적으로 구성하여 다양한 검증 시나리오 지원
5. **로깅**: 각 검증 단계별 상세한 로그 제공
6. **에러 핸들링**: 검증 실패 시 명확한 에러 처리

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

#### Infrastructure Docker Sentry 사용

chap16에서는 **infrastructure Docker Compose에 구성된 Self-hosted Sentry**를 사용합니다. 클라우드 Sentry 대신 온프레미스 환경에서 완전한 제어권을 가지고 에러 모니터링을 수행할 수 있습니다.

##### Infrastructure Sentry 구성 확인

```bash
# 1. Infrastructure Docker 환경 시작
cd infrastructure
docker-compose up -d

# 2. Sentry 웹 UI 접속
# http://localhost:9000

# 3. Sentry 상태 확인
docker-compose ps | grep sentry
```

##### Infrastructure Sentry 컨테이너 구성

```yaml
# infrastructure/docker-compose.yml에 구성된 Sentry 서비스들
services:
  # Sentry PostgreSQL - 에러 추적용 데이터베이스
  sentry-postgres:
    image: postgres:13
    container_name: sentry-postgres-primavera
    environment:
      POSTGRES_PASSWORD: sentrypassword
      POSTGRES_USER: sentry
      POSTGRES_DB: sentry

  # Sentry Web - 에러 추적 및 모니터링 웹 인터페이스
  sentry-web:
    image: sentry:24.1.0
    container_name: sentry-web-primavera
    ports:
      - "9000:9000"  # 웹 UI 접속 포트
    environment:
      SENTRY_SECRET_KEY: "primavera-sentry-secret-key-change-in-production"
      SENTRY_SINGLE_ORGANIZATION: 'true'

  # Sentry Cron - 백그라운드 작업 스케줄러
  sentry-cron:
    image: sentry:24.1.0
    command: sentry run cron

  # Sentry Worker - 백그라운드 작업 처리기
  sentry-worker:
    image: sentry:24.1.0
    command: sentry run worker
```

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

#### Local Infrastructure Sentry 설정

```yaml
# application-local.yml
sentry:
  dns: http://localhost:9000/api/1/project/1/store/  # Infrastructure Sentry URL
  environment: local-development
  servername: chap16-local
  release: 0.0.1-SNAPSHOT

# application.yml (Production)
sentry:
  dns: https://4084f8500752461897ebbfe3a067d36c@sentry.io/5166811  # 클라우드 Sentry (옵션)
  environment: production
  servername: chap16
  release: 0.0.1-SNAPSHOT
```

#### Self-hosted Sentry 장점

1. **데이터 프라이버시**: 에러 데이터가 외부로 전송되지 않음
2. **비용 절약**: 무제한 에러 수집 및 사용자 수
3. **커스터마이징**: 필요에 따른 Sentry 설정 변경 가능
4. **네트워크 격리**: 내부 네트워크에서만 접근 가능
5. **법적 요구사항**: 데이터 주권 및 컴플라이언스 준수

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
<!-- logback-spring.xml 설정 -->
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

## 🔐 Vault 통합 보안 자격증명 관리

### Spring Cloud Vault를 통한 AWS 자격증명 보안 관리

chap16에서는 **Spring Cloud Vault**를 통해 AWS S3 자격증명을 안전하게 관리합니다. 설정 파일에 민감한 정보를 노출하지 않고 중앙화된 보안 저장소에서 동적으로 자격증명을 로드합니다.

#### 1. Vault 설정 자동화

```bash
# AWS 자격증명을 Vault에 저장하는 스크립트 실행
chmod +x ../../infrastructure/vault-init.sh

# 환경변수 설정 후 실행
export AWS_ACCESS_KEY_ID="your-actual-access-key"
export AWS_SECRET_ACCESS_KEY="your-actual-secret-key" 
export AWS_S3_BUCKET_NAME="your-s3-bucket-name"

../../infrastructure/vault-init.sh
```

#### 2. Spring Boot Vault 통합 설정

```yaml
# application.yml - Vault 설정
spring:
  cloud:
    vault:
      host: ${VAULT_HOST:localhost}
      port: ${VAULT_PORT:8200}
      scheme: ${VAULT_SCHEME:http}
      authentication: TOKEN
      token: ${VAULT_TOKEN:primavera-dev-token}
      kv:
        enabled: true
        backend: secret
        profile-separator: '/'
        default-context: primavera
        application-name: chap16
    aws:
      credentials:
        # Vault에서 동적으로 로드되는 자격증명
        access-key: ${aws.credentials.access-key:${AWS_ACCESS_KEY_ID:}}
        secret-key: ${aws.credentials.secret-key:${AWS_SECRET_ACCESS_KEY:}}
      region:
        static: ${aws.region:${AWS_REGION:ap-northeast-2}}
      s3:
        bucket-name: ${aws.s3.bucket-name:${AWS_S3_BUCKET_NAME:primavera-bucket}}
        endpoint: ${aws.s3.endpoint:${AWS_S3_ENDPOINT:}}
        path-style-access: ${aws.s3.path-style-access:${AWS_S3_PATH_STYLE_ACCESS:false}}
```

#### 3. Vault에 저장되는 시크릿 구조

```bash
# Vault KV 경로: secret/primavera/chap16
vault kv get secret/primavera/chap16

# 저장되는 키-값 쌍들:
# aws.credentials.access-key: AKIA...
# aws.credentials.secret-key: xYz9...
# aws.region: ap-northeast-2
# aws.s3.bucket-name: primavera-bucket
# aws.s3.endpoint: (빈 값 또는 MinIO 엔드포인트)
# aws.s3.path-style-access: false
```

#### 4. 테스트 환경 Vault 통합

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class S3FileServiceVaultIntegrationTest {

    @Container
    static GenericContainer<?> vault = new GenericContainer<>("hashicorp/vault:1.15")
            .withExposedPorts(8200)
            .withEnv("VAULT_DEV_ROOT_TOKEN_ID", "primavera-dev-token");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Vault 엔드포인트 동적 설정
        registry.add("spring.cloud.vault.host", vault::getHost);
        registry.add("spring.cloud.vault.port", () -> vault.getMappedPort(8200));
        registry.add("spring.cloud.vault.token", () -> "primavera-dev-token");
    }

    @BeforeAll
    static void setupVaultSecrets() throws Exception {
        // TestContainers Vault에 AWS 자격증명 저장
        vault.execInContainer(
            "vault", "kv", "put", "secret/primavera/chap16",
            "aws.credentials.access-key=" + localstack.getAccessKey(),
            "aws.credentials.secret-key=" + localstack.getSecretKey(),
            "aws.region=" + localstack.getRegion(),
            "aws.s3.bucket-name=test-primavera-vault-bucket"
        );
    }
}
```

#### 5. 보안 이점

| 구분 | 기존 방식 | Vault 통합 |
|------|-----------|------------|
| **자격증명 저장** | 환경변수/설정파일 | 중앙화된 보안 저장소 |
| **접근 제어** | OS 수준 권한 | Vault 정책 기반 |
| **감사 로그** | 제한적 | 모든 접근 기록 |
| **자동 로테이션** | 수동 | 자동 시크릿 갱신 |
| **암호화** | 운영체제 의존 | 전송/저장 모두 암호화 |
| **중앙 관리** | 분산된 설정 | 통합 시크릿 관리 |

#### 6. 운영 환경 설정

```bash
# 운영환경 Vault 서버 설정
export VAULT_HOST=production-vault.company.com
export VAULT_PORT=8200 
export VAULT_SCHEME=https
export VAULT_TOKEN=$(cat /etc/vault/tokens/app-token)

# 애플리케이션 시작
./gradlew :chap16:bootRun
```

#### 7. Vault 시크릿 관리 명령어

```bash
# 시크릿 조회
vault kv get secret/primavera/chap16

# 시크릿 업데이트 (키 하나만)
vault kv patch secret/primavera/chap16 aws.s3.bucket-name=new-bucket-name

# 시크릿 삭제
vault kv delete secret/primavera/chap16

# 시크릿 히스토리 조회
vault kv get -version=1 secret/primavera/chap16
```

#### 8. 시크릿 로테이션 자동화

```bash
# AWS 자격증명 로테이션 스크립트
#!/bin/bash
NEW_ACCESS_KEY=$(aws iam create-access-key --user-name primavera-s3-user --query 'AccessKey.AccessKeyId' --output text)
NEW_SECRET_KEY=$(aws iam create-access-key --user-name primavera-s3-user --query 'AccessKey.SecretAccessKey' --output text)

vault kv patch secret/primavera/chap16 \
    aws.credentials.access-key="$NEW_ACCESS_KEY" \
    aws.credentials.secret-key="$NEW_SECRET_KEY"

# 기존 키 비활성화 (유예 기간 후)
OLD_ACCESS_KEY=$(vault kv get -field=aws.credentials.access-key secret/primavera/chap16)
aws iam delete-access-key --user-name primavera-s3-user --access-key-id "$OLD_ACCESS_KEY"
```

### Vault 통합의 핵심 가치

1. **Zero Trust 보안**: 애플리케이션이 민감한 정보를 직접 저장하지 않음
2. **감사 추적성**: 모든 시크릿 접근이 로그로 기록됨
3. **동적 자격증명**: 런타임에 필요한 시점에만 시크릿 로드
4. **중앙 집중화**: 모든 환경의 시크릿을 한 곳에서 관리
5. **자동화 친화적**: CI/CD 파이프라인과 자연스럽게 통합

---

## 🧪 AWS S3 Integration Test 사용법

### 🚀 테스트 실행 방법

#### 1. LocalStack으로 테스트 (기본값)

별도 설정 없이 테스트를 실행하면 LocalStack S3가 자동으로 시작됩니다:

```bash
./gradlew :chap16:test --tests S3FileServiceIntegrationTest
```

#### 2. Spring Properties로 실제 AWS S3 테스트

##### 방법 1: Gradle 명령행에서 System Properties 전달

```bash
./gradlew :chap16:test \
  --tests S3FileServiceIntegrationTest \
  -Daws.credentials.access-key=AKIA1234567890EXAMPLE \
  -Daws.credentials.secret-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY \
  -Daws.region=ap-northeast-2 \
  -Daws.s3.bucket-name=my-test-bucket
```

##### 방법 2: IDE에서 VM Options 설정

**IntelliJ IDEA:**
1. Run Configuration → VM Options에 추가:
```
-Daws.credentials.access-key=AKIA1234567890EXAMPLE
-Daws.credentials.secret-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
-Daws.region=ap-northeast-2
-Daws.s3.bucket-name=my-test-bucket
```

**Visual Studio Code:**
`.vscode/launch.json`에 설정:
```json
{
  "type": "java",
  "name": "S3 Integration Test",
  "request": "launch",
  "mainClass": "com.genius.primavera.application.aws.S3FileServiceIntegrationTest",
  "vmArgs": [
    "-Daws.credentials.access-key=AKIA1234567890EXAMPLE",
    "-Daws.credentials.secret-key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
    "-Daws.region=ap-northeast-2",
    "-Daws.s3.bucket-name=my-test-bucket"
  ]
}
```

#### 3. 환경변수로 AWS S3 테스트 (폴백)

```bash
# 환경변수 설정
export AWS_ACCESS_KEY_ID=AKIA1234567890EXAMPLE
export AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
export AWS_REGION=ap-northeast-2
export AWS_S3_BUCKET_NAME=my-test-bucket

# 테스트 실행
./gradlew :chap16:test --tests S3FileServiceIntegrationTest
```

#### 4. MinIO (S3 호환) 서버 테스트

```bash
./gradlew :chap16:test \
  --tests S3FileServiceIntegrationTest \
  -Daws.credentials.access-key=minioadmin \
  -Daws.credentials.secret-key=minioadmin \
  -Daws.region=us-east-1 \
  -Daws.s3.endpoint=http://localhost:9000 \
  -Daws.s3.path-style-access=true \
  -Daws.s3.bucket-name=test-bucket
```

### 📋 지원하는 설정 파라미터

| 파라미터 | Spring Property | 환경변수 | 기본값 | 설명 |
|----------|-----------------|----------|--------|------|
| Access Key | `aws.credentials.access-key` | `AWS_ACCESS_KEY_ID` | LocalStack 자동 생성 | AWS 액세스 키 |
| Secret Key | `aws.credentials.secret-key` | `AWS_SECRET_ACCESS_KEY` | LocalStack 자동 생성 | AWS 시크릿 키 |
| 리전 | `aws.region` | `AWS_REGION` | `ap-northeast-2` | AWS 리전 |
| 버킷명 | `aws.s3.bucket-name` | `AWS_S3_BUCKET_NAME` | `test-primavera-bucket` | S3 버킷 이름 |
| 엔드포인트 | `aws.s3.endpoint` | `AWS_S3_ENDPOINT` | (빈 값) | S3 엔드포인트 URL |
| Path Style | `aws.s3.path-style-access` | `AWS_S3_PATH_STYLE_ACCESS` | `false` (AWS), `true` (LocalStack) | Path-style 접근 여부 |

### 🔄 자동 선택 로직

테스트는 다음 우선순위로 설정을 적용합니다:

1. **Spring System Properties** (`-Daws.credentials.access-key=...`)
2. **환경변수** (`AWS_ACCESS_KEY_ID=...`)
3. **LocalStack** (기본값, TestContainers 자동 시작)

### ⚠️ 주의사항

#### 실제 AWS S3 테스트 시
- **버킷이 이미 존재해야 합니다** (테스트에서 자동 생성하지 않음)
- **적절한 IAM 권한이 필요합니다**:
  ```json
  {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Action": [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket"
        ],
        "Resource": [
          "arn:aws:s3:::your-test-bucket",
          "arn:aws:s3:::your-test-bucket/*"
        ]
      }
    ]
  }
  ```
- **요금이 발생할 수 있습니다** (소량이지만 PUT/GET 요청 비용)

#### LocalStack 테스트 시
- Docker가 실행 중이어야 합니다
- TestContainers가 자동으로 LocalStack을 시작/종료합니다
- 완전히 격리된 테스트 환경을 제공합니다

### 🧪 테스트 시나리오

각 실행 방식별로 다음 테스트들이 수행됩니다:

1. ✅ MultipartFile S3 업로드
2. ✅ InputStream S3 업로드  
3. ✅ S3 파일 다운로드
4. ✅ 존재하지 않는 파일 다운로드 (Empty 반환)
5. ✅ 파일 존재 여부 확인
6. ✅ 파일 목록 조회 (prefix 필터링)
7. ✅ 전체 파일 목록 조회
8. ✅ 파일 메타데이터 조회
9. ✅ 존재하지 않는 파일 메타데이터 조회
10. ✅ 파일 삭제
11. ✅ 존재하지 않는 파일 삭제

### 🏃‍♂️ 빠른 시작 예제

```bash
# 1. LocalStack으로 빠른 테스트
./gradlew :chap16:test --tests S3FileServiceIntegrationTest

# 2. 실제 AWS S3로 테스트 (버킷 준비 필요)
./gradlew :chap16:test \
  --tests S3FileServiceIntegrationTest \
  -Daws.credentials.access-key=YOUR_ACCESS_KEY \
  -Daws.credentials.secret-key=YOUR_SECRET_KEY \
  -Daws.s3.bucket-name=your-existing-bucket

# 3. MinIO 로컬 서버로 테스트
docker run -p 9000:9000 -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  minio/minio server /data --console-address ":9001"

./gradlew :chap16:test \
  --tests S3FileServiceIntegrationTest \
  -Daws.credentials.access-key=minioadmin \
  -Daws.credentials.secret-key=minioadmin \
  -Daws.s3.endpoint=http://localhost:9000 \
  -Daws.s3.path-style-access=true
```

### 🔐 Vault 설정 (선택사항)

테스트 실행 전 AWS 자격증명을 Vault에 저장하려면:

```bash
# Vault 설정 스크립트 실행
chmod +x ../../infrastructure/vault-init.sh
../../infrastructure/vault-init.sh
```

이 스크립트는 다음 작업을 수행합니다:
- Vault 서버 시작 (개발 모드)
- AWS 자격증명을 secret/primavera/chap16에 저장
- 테스트에서 자동으로 Vault에서 자격증명 읽어옴

### 📊 테스트 실행 결과

#### 기본 통합 테스트 (S3FileServiceIntegrationTest)

```bash
./gradlew :chap16:test --tests S3FileServiceIntegrationTest
```

실행 결과:
```
🔧 테스트 설정 확인:
   Access Key: test****
   Secret Key: test****
   Region: ap-northeast-2
   Bucket: test-primavera-bucket
   Endpoint: (AWS Default)
   환경: 🐳 LocalStack
✅ LocalStack 테스트 환경이 올바르게 설정되었습니다.
```

#### Vault 통합 시나리오 테스트 (S3FileServiceVaultIntegrationTest)

```bash
./gradlew :chap16:test --tests S3FileServiceVaultIntegrationTest
```

실행 결과:
```
🔐📋 Vault + application-test.yml 설정 확인:
   Access Key: test****
   Secret Key: test****
   Region: ap-northeast-2
   Bucket: test-primavera-bucket
   Endpoint: (AWS Default)
   환경: 🔐 Vault 통합 + 🐳 LocalStack
✅ LocalStack + application-test.yml 설정이 올바르게 구성되었습니다.
💡 실제 Vault 사용 시: ../../infrastructure/vault-init.sh 스크립트를 실행하세요.
```

**두 테스트 모두 동일한 application-test.yml 설정을 사용하며, Vault 테스트는 실제 Vault 통합 시나리오를 시뮬레이션합니다.**

### 🏗️ 깔끔한 Configuration Properties 바인딩

기존의 여러 `@Value` 어노테이션 대신 `TestAwsProperties` 객체로 깔끔하게 바인딩:

```java
// 기존 방식 (여러 @Value 어노테이션)
@Value("${spring.cloud.aws.credentials.access-key}")
private String accessKey;

@Value("${spring.cloud.aws.credentials.secret-key}")
private String secretKey;

// 새로운 방식 (깔끔한 객체 바인딩)
@Autowired
private TestAwsProperties testAwsProperties;

// 사용법
testAwsProperties.credentials().accessKey()
testAwsProperties.region().value()
testAwsProperties.s3().bucketName()
testAwsProperties.isLocalStack()
```

#### TestAwsProperties 구조

```java
@ConfigurationProperties(prefix = "spring.cloud.aws")
public record TestAwsProperties(
    Credentials credentials,  // access-key, secret-key
    Region region,           // static (Java 키워드이므로 @Name 사용)
    S3 s3                    // bucket-name, endpoint
) {
    // 편의 메서드들
    public boolean isLocalStack()    // LocalStack 사용 여부
    public boolean isRealAws()       // 실제 AWS 사용 여부
}
```

이제 다양한 환경에서 S3 통합 테스트를 유연하게 실행할 수 있습니다! 🎉

---

**🎓 학습 포인트**: 파일 처리는 엔터프라이즈 애플리케이션의 핵심 기능입니다. 검증, 변환, 모니터링과 함께 **보안 자격증명 관리**를 체계적으로 구현하면 안정적이고 확장 가능한 시스템을 구축할 수 있습니다.