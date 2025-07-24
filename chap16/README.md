## chap16 - 배포 및 모니터링

### 개요
이 챕터에서는 Spring Boot 애플리케이션의 배포 과정과 모니터링 방법에 대해 다룹니다. 컨테이너화, CI/CD 파이프라인 구축, 그리고 실시간 모니터링 시스템 설정 방법을 학습합니다.

### 주요 내용

#### 1. 도커를 이용한 컨테이너화
- **Dockerfile 작성**
  - 멀티스테이지 빌드 패턴 구현
  - 경량 베이스 이미지 선택 (JRE-alpine)
  - 레이어 최적화
  
```dockerfile
# 빌드 스테이지
FROM gradle:8.12.1-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# 실행 스테이지
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8443
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- **Docker Compose 환경 구성**
  - 애플리케이션과 데이터베이스 연결
  - 개발/테스트/운영 환경별 설정
  - 볼륨 마운트를 통한 데이터 영속성

#### 2. CI/CD 파이프라인 구축
- **GitHub Actions 워크플로우**
  - 자동 빌드, 테스트 및 배포
  - 다양한 환경에서의 테스트 매트릭스
  - 환경별 배포 자동화

```yaml
# .github/workflows/ci-cd.yml
name: Java CI/CD with Gradle

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Build with Gradle
      uses: gradle/gradle-build-action@v2
      with:
        arguments: build
        
    - name: Run tests
      run: ./gradlew test
      
    - name: Build and publish Docker image
      if: github.ref == 'refs/heads/main'
      env:
        DOCKER_USERNAME: ${{ secrets.DOCKER_USERNAME }}
        DOCKER_PASSWORD: ${{ secrets.DOCKER_PASSWORD }}
      run: |
        ./gradlew jib --image=primavera/app:latest -Djib.to.auth.username=$DOCKER_USERNAME -Djib.to.auth.password=$DOCKER_PASSWORD
        
    - name: Deploy to production
      if: github.ref == 'refs/heads/main'
      env:
        SSH_PRIVATE_KEY: ${{ secrets.SSH_PRIVATE_KEY }}
        SERVER_IP: ${{ secrets.SERVER_IP }}
      run: |
        echo "$SSH_PRIVATE_KEY" > deploy_key
        chmod 600 deploy_key
        ssh -i deploy_key -o StrictHostKeyChecking=no user@$SERVER_IP "cd /opt/primavera && docker-compose pull && docker-compose up -d"
```

- **GitHub Actions 기능**
  - 다양한 이벤트 트리거 (push, pull request, 스케줄 등)
  - 환경 변수 및 시크릿 관리
  - 캐싱을 통한 빌드 속도 향상
  - 병렬 작업 실행 및 작업 간 의존성 관리

- **환경별 배포 전략**
  - 개발 환경: Pull Request 시 자동 배포
  - 스테이징 환경: develop 브랜치 배포
  - 프로덕션 환경: main 브랜치 배포 및 승인 절차

```yaml
# 환경별 배포를 위한 워크플로우 예시
name: Environment Deployments

on:
  push:
    branches: [ develop, main ]
  workflow_dispatch:
    inputs:
      environment:
        description: 'Environment to deploy to'
        required: true
        default: 'development'
        type: choice
        options:
        - development
        - staging
        - production

jobs:
  deploy:
    name: Deploy to ${{ github.event.inputs.environment || (github.ref == 'refs/heads/main' && 'production') || 'development' }}
    runs-on: ubuntu-latest
    environment: ${{ github.event.inputs.environment || (github.ref == 'refs/heads/main' && 'production') || 'development' }}
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Build with profile
      run: ./gradlew build -Pprofile=${{ github.event.inputs.environment || (github.ref == 'refs/heads/main' && 'production') || 'development' }}
      
    - name: Deploy to environment
      env:
        DEPLOY_TOKEN: ${{ secrets.DEPLOY_TOKEN }}
      run: |
        echo "Deploying to ${{ github.event.inputs.environment || (github.ref == 'refs/heads/main' && 'production') || 'development' }}"
        # 실제 배포 스크립트
```

- **Travis CI 설정**
  - 자동 빌드 및 테스트
  - 테스트 커버리지 측정
  - Docker Hub에 이미지 자동 푸시

```yaml
# .travis.yml
language: java
jdk:
  - openjdk21

services:
  - docker

before_install:
  - chmod +x gradlew

script:
  - ./gradlew clean build
  - ./gradlew jacocoTestReport

after_success:
  - bash <(curl -s https://codecov.io/bash)
  - docker build -t primavera/app:latest .
  - echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
  - docker push primavera/app:latest
```

- **배포 자동화**
  - 단계별 배포 전략 (개발 → 스테이징 → 운영)
  - 블루-그린 배포 구현
  - 롤백 메커니즘

#### 3. 애플리케이션 모니터링
- **Spring Boot Actuator**
  - 주요 엔드포인트 활성화 및 보안 설정
  - 사용자 정의 Health 지표 구현
  - JMX 통합

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT 1");
            ps.executeQuery();
            return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("timestamp", LocalDateTime.now())
                    .build();
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("database", "Unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
```

- **Prometheus 통합**
  - 메트릭 수집 설정
  - 사용자 정의 메트릭 생성
  - 알림 규칙 설정

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['app:8443']
```

- **Grafana 대시보드**
  - 시스템 모니터링 대시보드 구성
  - 애플리케이션 성능 시각화
  - 사용자 행동 분석 패널

#### 4. 로깅 및 예외 추적
- **중앙 집중식 로깅 시스템**
  - ELK 스택 (Elasticsearch, Logstash, Kibana) 구성
  - 로그 포맷 표준화
  - 로그 수준 동적 조정

- **Sentry 통합**
  - 실시간 오류 추적
  - 스택 트레이스 분석
  - 오류 알림 설정

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    private final SentryClient sentryClient;
    
    public GlobalExceptionHandler(SentryClient sentryClient) {
        this.sentryClient = sentryClient;
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, WebRequest request) {
        // Sentry에 예외 보고
        EventBuilder eventBuilder = new EventBuilder()
                .withMessage(ex.getMessage())
                .withLevel(Event.Level.ERROR)
                .withExtra("url", request.getDescription(false));
        
        sentryClient.sendEvent(eventBuilder);
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

#### 5. 성능 최적화
- **JVM 튜닝**
  - 힙 메모리 설정 최적화
  - 가비지 컬렉터 선택 및 설정
  - JVM 플래그 최적화

- **애플리케이션 성능 분석**
  - JProfiler를 이용한 병목 지점 식별
  - 캐싱 전략 구현
  - 데이터베이스 쿼리 최적화

#### 6. GitHub 기반 DevOps 환경 구축
- **이슈 및 프로젝트 관리**
  - GitHub Issues를 통한 작업 추적
  - 프로젝트 칸반 보드 설정
  - 이슈 템플릿 및 라벨 활용

- **코드 품질 관리**
  - GitHub Actions를 이용한 코드 분석
  - SonarCloud 통합
  - 코드 커버리지 리포트 생성

```yaml
# .github/workflows/code-quality.yml
name: Code Quality

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  sonarcloud:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
      with:
        fetch-depth: 0
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Cache SonarCloud packages
      uses: actions/cache@v3
      with:
        path: ~/.sonar/cache
        key: ${{ runner.os }}-sonar
        restore-keys: ${{ runner.os }}-sonar
        
    - name: Build and analyze
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
      run: ./gradlew build jacocoTestReport sonarqube --info
```

- **자동화된 테스트 환경**
  - 정기적인 통합 테스트 실행
  - 성능 테스트 자동화
  - 테스트 결과 보고서 생성 및 공유

### GitHub Actions를 이용한 CI/CD 파이프라인 구축
GitHub Actions를 사용하여 Spring Boot 애플리케이션의 빌드, Docker 이미지 생성, AWS ECR 푸시, 그리고 EKS 배포를 자동화할 수 있습니다. 아래는 이를 구현하는 방법에 대한 설명과 예제입니다.

#### GitHub Actions 워크플로우 예제
다음은 Java 빌드, Docker 빌드, AWS ECR 푸시 및 EKS 배포를 포함한 GitHub Actions 워크플로우 예제입니다:

```yaml
# .github/workflows/ci-cd.yml
name: Java CI/CD with AWS EKS

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: Build with Gradle
      run: ./gradlew build

    - name: Run tests
      run: ./gradlew test

    - name: Log in to Amazon ECR
      id: login-ecr
      uses: aws-actions/amazon-ecr-login@v1

    - name: Build, tag, and push Docker image
      env:
        ECR_REGISTRY: ${{ secrets.AWS_ACCOUNT_ID }}.dkr.ecr.${{ secrets.AWS_REGION }}.amazonaws.com
        ECR_REPOSITORY: primavera-app
        IMAGE_TAG: ${{ github.sha }}
      run: |
        docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v2
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ secrets.AWS_REGION }}

    - name: Deploy to EKS
      env:
        ECR_REGISTRY: ${{ secrets.AWS_ACCOUNT_ID }}.dkr.ecr.${{ secrets.AWS_REGION }}.amazonaws.com
        ECR_REPOSITORY: primavera-app
        IMAGE_TAG: ${{ github.sha }}
      run: |
        kubectl set image deployment/primavera-app primavera-app=$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
        kubectl rollout status deployment/primavera-app
```

#### 주요 단계 설명
1. **Java 빌드 및 테스트**:
   - `actions/setup-java`를 사용하여 JDK 21 환경을 설정합니다.
   - Gradle을 사용하여 애플리케이션을 빌드하고 테스트를 실행합니다.

2. **Docker 이미지 생성 및 ECR 푸시**:
   - `aws-actions/amazon-ecr-login`을 사용하여 Amazon ECR에 로그인합니다.
   - Docker 이미지를 빌드하고 ECR에 푸시합니다.

3. **AWS EKS 배포**:
   - `aws-actions/configure-aws-credentials`를 사용하여 AWS 자격 증명을 설정합니다.
   - `kubectl` 명령어를 사용하여 EKS 클러스터에 새 이미지를 배포합니다.

#### 실습 과제
1. GitHub Actions 워크플로우 파일을 생성하고 위의 단계를 구현하세요.
2. AWS ECR 및 EKS 설정을 완료하고, 배포를 테스트하세요.
3. `kubectl`을 사용하여 배포 상태를 확인하고 롤백 메커니즘을 설정하세요.

### ArgoCD를 이용한 EKS 배포
ArgoCD는 Kubernetes 애플리케이션을 GitOps 방식으로 관리하는 도구입니다. 이 섹션에서는 ArgoCD를 사용하여 Spring Boot 애플리케이션을 EKS에 배포하는 방법을 설명합니다. AWS 로드밸런서 도메인 인증서와 Helm 설정이 이미 준비된 상태를 가정합니다.

#### ArgoCD 애플리케이션 설정
ArgoCD를 통해 애플리케이션을 배포하려면 다음과 같은 설정이 필요합니다:

1. **Git 리포지토리 준비**:
   - 애플리케이션의 Kubernetes 매니페스트 또는 Helm 차트를 Git 리포지토리에 저장합니다.

2. **ArgoCD 애플리케이션 생성**:
   - ArgoCD CLI 또는 UI를 사용하여 애플리케이션을 생성합니다.

```yaml
# argocd-application.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: primavera-app
  namespace: argocd
spec:
  project: default
  source:
    repoURL: 'https://github.com/your-repo/primavera.git'
    targetRevision: HEAD
    path: helm/primavera-app
  destination:
    server: 'https://kubernetes.default.svc'
    namespace: primavera
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
```

3. **Helm 차트 구성**:
   - `values.yaml` 파일에서 도메인 인증서와 관련된 값을 설정합니다.

```yaml
# helm/primavera-app/values.yaml
service:
  type: LoadBalancer
  annotations:
    service.beta.kubernetes.io/aws-load-balancer-ssl-cert: "arn:aws:acm:region:account-id:certificate/certificate-id"
    service.beta.kubernetes.io/aws-load-balancer-backend-protocol: "http"
    service.beta.kubernetes.io/aws-load-balancer-ssl-ports: "443"
  ports:
    - name: http
      port: 80
      targetPort: 8080
    - name: https
      port: 443
      targetPort: 8080
```

4. **ArgoCD CLI를 사용한 애플리케이션 동기화**:
   - CLI를 사용하여 애플리케이션을 동기화합니다.

```bash
argocd app create primavera-app \
  --repo https://github.com/your-repo/primavera.git \
  --path helm/primavera-app \
  --dest-server https://kubernetes.default.svc \
  --dest-namespace primavera

argocd app sync primavera-app
```

#### 주요 단계 설명
1. **GitOps 방식**:
   - Kubernetes 매니페스트 또는 Helm 차트를 Git 리포지토리에 저장하고, ArgoCD가 이를 자동으로 동기화합니다.

2. **자동화된 동기화**:
   - `syncPolicy`를 통해 애플리케이션 상태를 자동으로 동기화하고, 불필요한 리소스를 제거합니다.

3. **Helm 차트 활용**:
   - Helm 차트를 사용하여 Kubernetes 리소스를 템플릿화하고, AWS 로드밸런서와 도메인 인증서를 쉽게 설정합니다.

#### 실습 과제
1. ArgoCD CLI 또는 UI를 사용하여 애플리케이션을 생성하고 동기화하세요.
2. Helm 차트를 수정하여 도메인 인증서와 관련된 설정을 적용하세요.
3. ArgoCD 대시보드에서 애플리케이션 상태를 확인하고, 동기화 상태를 유지하세요.

### 참고 자료
- [Docker 공식 문서](https://docs.docker.com/)
- [GitHub Actions 문서](https://docs.github.com/en/actions)
- [Spring Boot Actuator 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Prometheus 공식 문서](https://prometheus.io/docs/introduction/overview/)
- [Grafana 대시보드 갤러리](https://grafana.com/grafana/dashboards/)
- [Sentry Java SDK 문서](https://docs.sentry.io/platforms/java/)
- [SonarCloud 문서](https://sonarcloud.io/documentation)
- [ArgoCD 공식 문서](https://argo-cd.readthedocs.io/)
- [Helm 공식 문서](https://helm.sh/docs/)
- [AWS 로드밸런서 컨트롤러](https://kubernetes-sigs.github.io/aws-load-balancer-controller/)

### 참고 사항

ArgoCD, EKS, AWS 기반의 로드밸런서(LB) 및 Route53과 같은 고급 AWS 서비스 설정은 이 강의의 범위를 벗어납니다. 따라서 해당 내용은 자세히 다루지 않습니다. 하지만, 관련된 공식 문서와 추가 학습 자료를 참고하여 실습을 진행할 수 있습니다. 아래는 참고할 수 있는 주요 자료들입니다:

- [ArgoCD 공식 문서](https://argo-cd.readthedocs.io/)
- [AWS EKS 공식 문서](https://docs.aws.amazon.com/eks/latest/userguide/what-is-eks.html)
- [AWS 로드밸런서 컨트롤러](https://kubernetes-sigs.github.io/aws-load-balancer-controller/)
- [AWS Route53 공식 문서](https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/Welcome.html)
