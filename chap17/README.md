# Chapter 18: Spring Batch와 CI/CD 배포

## 개요
이 챕터는 Spring Batch를 활용한 대용량 데이터 처리와 CI/CD 파이프라인 구축에 대해 다룹니다. CSV 파일을 읽어 데이터를 변환하고 데이터베이스에 저장하는 배치 작업을 구현합니다.

## 주요 기능

### 1. Spring Batch 구성
- **Job**: `importUserJob` - 사용자 데이터를 가져와 처리하는 배치 작업
- **Step**: `step1` - 단일 스텝으로 구성된 배치 프로세스
- **Chunk Size**: 10개 단위로 데이터 처리

### 2. 배치 처리 흐름
```
CSV 파일 읽기 → 데이터 변환 (대문자 변환) → 데이터베이스 저장 → 결과 검증
```

### 3. 구성 요소

#### ItemReader
- **FlatFileItemReader**: CSV 파일(`sample-data.csv`)에서 Person 데이터를 읽음
- 구분자: 쉼표(,)
- 필드: firstName, lastName

#### ItemProcessor
- Person 객체의 firstName과 lastName을 대문자로 변환
- 변환 과정을 로그로 출력

#### ItemWriter
- **JdbcBatchItemWriter**: 변환된 데이터를 PEOPLE 테이블에 저장
- SQL: `INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME) VALUES (:firstName, :lastName)`

#### JobExecutionListener
- 배치 작업 완료 후 데이터베이스에서 저장된 데이터를 조회하여 검증

## 기술 스택
- Spring Boot 3.x
- Spring Batch
- MariaDB
- Lombok
- OpenCSV
- Open Korean Text (한국어 자연어 처리)
- ModelMapper

## 프로젝트 구조
```
chap18/
├── src/main/java/com/genius/primavera/
│   ├── CiCdDeploymentApplication.java    # 메인 애플리케이션 및 배치 설정
│   └── domain/
│       └── Person.java                    # Person 도메인 모델
├── src/main/resources/
│   ├── application.yml                    # 애플리케이션 설정
│   └── sample-data.csv                    # 샘플 데이터 파일
└── build.gradle                           # 빌드 설정
```

## 실행 방법

### 1. 데이터베이스 준비
```sql
-- PEOPLE 테이블 생성 (자동 생성되지 않는 경우)
CREATE TABLE PEOPLE (
    FIRST_NAME VARCHAR(100),
    LAST_NAME VARCHAR(100)
);
```

### 2. 애플리케이션 실행
```bash
./gradlew :chap18:bootRun
```

### 3. 실행 결과
- sample-data.csv의 데이터가 대문자로 변환되어 데이터베이스에 저장됨
- 배치 작업 완료 후 저장된 데이터가 콘솔에 출력됨

## 주요 특징

### 1. 함수형 Bean 등록
- XML이나 어노테이션 대신 프로그래밍 방식으로 Bean 등록
- `GenericApplicationContext`를 사용한 동적 Bean 정의

### 2. 배치 처리 최적화
- Chunk 기반 처리로 메모리 효율성 향상
- 트랜잭션 관리를 통한 데이터 무결성 보장

### 3. CI/CD 친화적 설계
- 명령줄 실행 지원 (SpringApplication.exit)
- 배치 작업 완료 후 시스템 종료

## 확장 가능성
- 다양한 파일 형식 지원 (Excel, JSON 등)
- 병렬 처리 및 파티셔닝
- 오류 처리 및 재시도 메커니즘
- 스케줄링 통합 (Spring Scheduler, Quartz)
- 모니터링 및 알림 기능

## 로깅 설정
- Spring Framework: DEBUG 레벨
- 애플리케이션 코드: DEBUG 레벨
- 배치 처리 과정의 상세한 로그 출력