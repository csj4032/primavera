# Primavera 데이터베이스 최적화 가이드

## 개요

Primavera 프로젝트의 데이터베이스 구조를 기존 7개에서 3개로 최적화하여 관리 효율성을 극대화하면서 교육적 목적을 유지합니다.

## 최적화 전략

### 기존 구조 (7개 데이터베이스)
- `primavera_basic` - chap03-05
- `primavera_mybatis` - chap06-11  
- `primavera_mybatis_board` - chap12-13
- `primavera_jpa_advanced` - chap14-15
- `primavera_jpa_board` - chap16-17
- `primavera_microservices` - chap18
- `primavera_test` - 테스트 전용

### 최적화된 구조 (3개 데이터베이스)

#### 1. **primavera_development** (개발/학습용)
- **목적**: chap01-17의 모든 교육용 테이블 통합
- **특징**: 
  - 테이블명 접두사로 기능별 분리 (BASIC_, MYBATIS_, JPA_, BOARD_)
  - 공통 사용자/권한 테이블 공유
  - 교육적 독립성 유지
- **환경**: local, test

#### 2. **primavera_microservices** (운영용)
- **목적**: chap18 실제 마이크로서비스 아키텍처
- **특징**:
  - 독립적 서비스별 스키마
  - 운영환경 최적화 (버전 관리, 트랜잭션 로그)
  - 확장성 고려 설계
- **환경**: local, test, prod

#### 3. **primavera_test** (테스트 전용)
- **목적**: TestContainers 및 CI/CD 테스트
- **특징**:
  - 경량화된 스키마
  - 빠른 테스트 실행 최적화
  - 테스트 데이터 사전 준비
- **환경**: test

## 환경별 데이터베이스 구성

### Local 환경
```yaml
databases:
  - primavera_development  # 모든 학습용 챕터
  - primavera_microservices # 마이크로서비스 실습
  - primavera_test         # 로컬 테스트
```

### Test 환경 (TestContainers)
```yaml
databases:
  - primavera_test         # 통합 테스트용 경량 DB
```

### Production 환경
```yaml
databases:
  - primavera_production   # 운영용 애플리케이션 DB
  - primavera_microservices # 마이크로서비스 운영 DB  
  - primavera_monitoring   # 로그/모니터링 전용 DB
```

## 테이블 구조 및 명명 규칙

### Development DB 테이블 접두사
- `BASIC_*` - chap03-05 기본 예제
- `MYBATIS_*` - chap06-11 MyBatis 예제
- `BOARD_*` - chap12-13 게시판 시스템
- `JPA_*` - chap14-15 JPA 고급 매핑
- `FILE_*` - chap16-17 파일 처리
- 공통 테이블: `USERS`, `ROLES`, `USER_ROLES` (접두사 없음)

### 공통 테이블 설계 원칙
1. **사용자 관리**: 모든 챕터에서 공통 사용
2. **권한 관리**: 통합된 RBAC 시스템
3. **소셜 로그인**: OAuth2 연동 정보
4. **시퀀스 관리**: 통합 시퀀스 테이블

## 마이그레이션 가이드

### 1단계: 새로운 스키마 배포
```bash
# Local 환경
mysql -u primavera -p < infrastructure/sql/init-unified-local.sql

# Test 환경  
mysql -u primavera -p < infrastructure/sql/init-unified-test.sql

# Production 환경
mysql -u primavera -p < infrastructure/sql/init-unified-prod.sql
```

### 2단계: 애플리케이션 설정 업데이트
각 챕터의 application.yml 파일에서 데이터베이스 URL 변경:

```yaml
# Before
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/primavera_basic

# After  
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/primavera_development
```

### 3단계: 기존 데이터 마이그레이션 (필요시)
```sql
-- 예시: BASIC 테이블 데이터 이전
INSERT INTO primavera_development.BASIC_WINNERS 
SELECT * FROM primavera_basic.WINNERS;
```

## 성능 최적화

### 인덱스 전략
1. **복합 인덱스**: 자주 함께 조회되는 컬럼들
2. **파티셔닝**: 로그 테이블 월별 파티션
3. **풀텍스트 인덱스**: 게시글 제목/내용 검색

### 쿼리 최적화
1. **테이블 접두사 활용**: 챕터별 데이터 분리
2. **공통 테이블 조인**: USERS 테이블 중심 정규화
3. **캐시 전략**: 자주 조회되는 권한 정보

## 보안 고려사항

### Production 환경
1. **계정 분리**: 
   - `primavera_app` - 애플리케이션 전용
   - `primavera_ms` - 마이크로서비스 전용
   - `primavera_monitor` - 모니터링 전용

2. **권한 최소화**: 필요한 권한만 부여
3. **감사 로그**: 모든 데이터 변경 추적
4. **데이터 암호화**: 민감 정보 암호화 저장

### 백업 전략
```bash
# 개발용 DB 백업
mysqldump primavera_development > backup_dev_$(date +%Y%m%d).sql

# 운영용 DB 백업  
mysqldump primavera_production > backup_prod_$(date +%Y%m%d).sql

# 마이크로서비스 DB 백업
mysqldump primavera_microservices > backup_ms_$(date +%Y%m%d).sql
```

## 모니터링 및 유지보수

### 주요 모니터링 지표
1. **연결 수**: 동시 연결 수 모니터링
2. **쿼리 성능**: 슬로우 쿼리 로그 분석
3. **테이블 크기**: 파티션 관리
4. **인덱스 사용률**: 불필요한 인덱스 정리

### 정기 유지보수 작업
```sql
-- 테이블 분석 (주간)
ANALYZE TABLE USERS, POSTS, COMMENTS;

-- 파티션 관리 (월간)
ALTER TABLE SYSTEM_LOGS ADD PARTITION (
  PARTITION p202501 VALUES LESS THAN (TO_DAYS('2025-02-01'))
);

-- 오래된 로그 정리 (일간)
DELETE FROM SYSTEM_LOGS 
WHERE CREATED_AT < DATE_SUB(NOW(), INTERVAL 90 DAY);
```

## 장점 요약

### 1. 관리 효율성
- **3개 DB로 관리 복잡도 60% 감소**
- 통합된 사용자/권한 관리
- 일관된 스키마 구조

### 2. 교육적 가치
- 챕터별 독립성 유지 (테이블 접두사)
- 점진적 학습 과정 보존
- 실제 운영환경 시뮬레이션

### 3. 성능 최적화
- 적절한 인덱싱 전략
- 파티셔닝으로 대용량 데이터 관리
- 환경별 최적화된 구조

### 4. 보안 강화
- 계정별 권한 분리
- 감사 로그 및 보안 이벤트 추적
- 운영환경 보안 최적화

이 최적화된 구조를 통해 **최소 3개의 데이터베이스로 모든 환경(local, test, prod)을 효율적으로 관리**할 수 있습니다.