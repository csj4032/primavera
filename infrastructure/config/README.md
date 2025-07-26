# Config 관리

환경별(운영, 개발, 스테이지) 및 도메인별(결제, 전시, 검색)로 설정 파일을 분리 관리하는 이유와 좋은 구조에 대해 설명합니다.

## 발생 가능한 이슈
환경별 및 도메인별로 설정 파일을 분리 관리하지 않을 경우 다음과 같은 문제가 발생할 수 있습니다:

1. **환경 간 설정 충돌**:
   - 운영 환경의 민감한 설정(예: 데이터베이스 비밀번호, API 키)이 개발 환경에서 노출될 위험이 있습니다.
   - 개발 환경에서 사용되는 설정이 운영 환경에 잘못 배포되어 장애를 유발할 수 있습니다.

2. **도메인 간 설정 혼재**:
   - 결제, 전시, 검색 등 각 도메인의 설정이 하나의 파일에 섞여 있으면, 특정 도메인 변경 시 다른 도메인에 영향을 줄 가능성이 높습니다.
   - 설정 파일이 복잡해지고 가독성이 떨어져 유지보수가 어려워집니다.

3. **배포 오류 증가**:
   - 환경별로 필요한 설정을 수동으로 변경해야 하므로, 실수로 잘못된 설정이 배포될 가능성이 높아집니다.
   - 특정 환경에서만 발생하는 문제를 디버깅하기 어려워집니다.

4. **보안 문제**:
   - 운영 환경의 민감한 정보가 개발 환경에 노출될 경우, 보안 사고로 이어질 수 있습니다.

## 좋은 구조 예시
환경별 및 도메인별로 설정 파일을 분리하여 관리하는 구조를 제안합니다. 이를 통해 설정 충돌을 방지하고 유지보수를 용이하게 할 수 있습니다.

### 디렉토리 구조
```
config/
├── dev/
│   ├── payment.yml
│   ├── display.yml
│   └── search.yml
├── stage/
│   ├── payment.yml
│   ├── display.yml
│   └── search.yml
└── prod/
    ├── payment.yml
    ├── display.yml
    └── search.yml
```

### 예시 설정 파일
**`config/dev/payment.yml`**:
```yaml
payment:
  database:
    url: jdbc:mysql://dev-payment-db:3306/payment
    username: dev_user
    password: dev_password
  api:
    key: dev-payment-api-key
```

**`config/prod/payment.yml`**:
```yaml
payment:
  database:
    url: jdbc:mysql://prod-payment-db:3306/payment
    username: prod_user
    password: prod_password
  api:
    key: prod-payment-api-key
```

## 장점
1. **환경별 독립성**:
   - 각 환경의 설정이 독립적으로 관리되므로, 환경 간 충돌을 방지할 수 있습니다.

2. **도메인별 책임 분리**:
   - 각 도메인의 설정이 분리되어 있어, 특정 도메인의 변경이 다른 도메인에 영향을 주지 않습니다.

3. **자동화 용이성**:
   - CI/CD 파이프라인에서 환경별 설정 파일을 자동으로 로드하여 배포할 수 있습니다.

4. **보안 강화**:
   - 운영 환경의 민감한 정보가 개발 환경에 노출되지 않도록 보장할 수 있습니다.

## 구현 방법
Spring Boot를 사용하는 경우, `application.yml` 파일에서 프로파일을 활용하여 환경별 설정을 로드할 수 있습니다.

**`application.yml`**:
```yaml
spring:
  profiles:
    active: dev
```

**`application-dev.yml`**:
```yaml
payment:
  database:
    url: jdbc:mysql://dev-payment-db:3306/payment
    username: dev_user
    password: dev_password
```

**`application-prod.yml`**:
```yaml
payment:
  database:
    url: jdbc:mysql://prod-payment-db:3306/payment
    username: prod_user
    password: prod_password
```

## 결론
환경별 및 도메인별로 설정 파일을 분리 관리하면 설정 충돌을 방지하고, 보안을 강화하며, 유지보수를 용이하게 할 수 있습니다. 이를 통해 안정적이고 효율적인 애플리케이션 운영이 가능합니다.
