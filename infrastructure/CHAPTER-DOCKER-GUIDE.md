# 📚 Primavera Chapter별 Docker Compose 실행 가이드

## 🎯 개요

각 챕터의 메인 클래스를 실행하기 위한 Docker Compose 명령어와 설정 가이드입니다.

---

## 📋 Chapter 01-05: Spring Boot 기초

### 🐳 Docker Compose 환경
```bash
# 인프라 시작
cd infrastructure
docker-compose -f docker-compose.basic.yml up -d

# 상태 확인
docker-compose -f docker-compose.basic.yml ps

# 로그 확인
docker-compose -f docker-compose.basic.yml logs -f mariadb
```

### 🚀 메인 클래스 실행
```bash
# Chapter 01: SpringBootStarterApplication
./gradlew :chap01:bootRun -Dspring.profiles.active=local

# Chapter 02: ConfigurationDependencyApplication
./gradlew :chap02:bootRun -Dspring.profiles.active=local

# Chapter 03: MvcAopApplication
./gradlew :chap03:bootRun -Dspring.profiles.active=local

# Chapter 04: DataAccessApplication
./gradlew :chap04:bootRun -Dspring.profiles.active=local

# Chapter 05: MyBatisLoggingApplication
./gradlew :chap05:bootRun -Dspring.profiles.active=local
```

### 📊 사용 가능한 서비스
- **MariaDB**: `localhost:3308`
- **데이터베이스**: `primavera`, `primavera_basic`
- **접속 정보**: `primavera/primavera`

---

## 📋 Chapter 06-11: 웹 개발 & MyBatis

### 🐳 Docker Compose 환경
```bash
# 기존 환경 종료 (포트 충돌 방지)
cd infrastructure
docker-compose -f docker-compose.basic.yml down

# MyBatis 환경 시작
docker-compose -f docker-compose.mybatis.yml up -d

# 상태 확인
docker-compose -f docker-compose.mybatis.yml ps
```

### 🚀 메인 클래스 실행
```bash
# Chapter 06: ValidationApplication
./gradlew :chap06:bootRun -Dspring.profiles.active=local

# Chapter 07: ThymeleafWebApplication
./gradlew :chap07:bootRun -Dspring.profiles.active=local
# 접속: http://localhost:8080

# Chapter 08: SecurityFilterApplication
./gradlew :chap08:bootRun -Dspring.profiles.active=local

# Chapter 09: SpringSecurityBasicApplication
./gradlew :chap09:bootRun -Dspring.profiles.active=local
# 로그인: admin@primavera.com / password

# Chapter 10: OAuth2SocialLoginApplication
./gradlew :chap10:bootRun -Dspring.profiles.active=local
# OAuth2 로그인 지원 (Google, Facebook, GitHub, Kakao)

# Chapter 11: BoardSystemApplication
./gradlew :chap11:bootRun -Dspring.profiles.active=local
# 게시판: http://localhost:8080/board
```

### 📊 사용 가능한 서비스
- **MariaDB**: `localhost:3308`
- **데이터베이스**: `primavera_mybatis`, `primavera_mybatis_board`
- **기본 계정**: `admin@primavera.com / password`

---

## 📋 Chapter 12-13: 고급 게시판 & 보안

### 🐳 Docker Compose 환경
```bash
# 기존 환경 종료
cd infrastructure
docker-compose -f docker-compose.mybatis.yml down

# 게시판 + Vault 환경 시작
docker-compose -f docker-compose.board.yml up -d

# Vault 초기화 확인
docker-compose -f docker-compose.board.yml logs vault-init

# 상태 확인
docker-compose -f docker-compose.board.yml ps
```

### 🚀 메인 클래스 실행
```bash
# Vault 토큰 설정 (필수)
export VAULT_TOKEN=primavera-dev-token
export VAULT_ADDR=http://localhost:8200

# Chapter 12: HierarchicalCommentApplication
./gradlew :chap12:bootRun -Dspring.profiles.active=local
# 계층형 댓글 시스템: http://localhost:8080

# Chapter 13: AdvancedAuthorizationApplication
# SSL 비활성화 옵션 (인증서 이슈 시)
./gradlew :chap13:bootRun --args='--server.ssl.enabled=false --server.port=8013 --spring.profiles.active=local'
# 접속: http://localhost:8013/login
```

### 📊 사용 가능한 서비스
- **MariaDB**: `localhost:3308`
- **HashiCorp Vault**: `localhost:8200`
- **데이터베이스**: `primavera_mybatis_board`
- **Vault Token**: `primavera-dev-token`

### 🔐 Vault 설정 확인
```bash
# Vault 상태 확인
curl -H "X-Vault-Token: primavera-dev-token" http://localhost:8200/v1/sys/health

# 저장된 시크릿 확인
curl -H "X-Vault-Token: primavera-dev-token" \
     http://localhost:8200/v1/secret/data/AdvancedAuthorizationApplication/local
```

---

## 📋 Chapter 14-17: JPA 고급 & 파일처리

### 🐳 Docker Compose 환경
```bash
# 기존 환경 종료
cd infrastructure
docker-compose -f docker-compose.board.yml down

# JPA + Redis 환경 시작
docker-compose -f docker-compose.jpa.yml up -d

# Redis 상태 확인
docker exec -it redis-primavera-jpa redis-cli ping

# 상태 확인
docker-compose -f docker-compose.jpa.yml ps
```

### 🚀 메인 클래스 실행
```bash
# Chapter 14: JpaAdvancedMappingApplication
./gradlew :chap14:bootRun -Dspring.profiles.active=local
# JPA 고급 매핑: http://localhost:8080/api/companies

# Chapter 15: ReactiveProgrammingApplication
./gradlew :chap15:bootRun -Dspring.profiles.active=local
# 리액티브 API: http://localhost:8080/api/reactive/users

# Chapter 16: FileProcessingMonitoringApplication
./gradlew :chap16:bootRun -Dspring.profiles.active=local
# 파일 업로드: http://localhost:8080/files/upload
# 모니터링: http://localhost:8080/actuator/metrics

# Chapter 17: CiCdDeploymentApplication
./gradlew :chap17:bootRun -Dspring.profiles.active=local
# CI/CD 대시보드: http://localhost:8080/deployment/history
```

### 📊 사용 가능한 서비스
- **MariaDB**: `localhost:3308`
- **Redis**: `localhost:6380`
- **데이터베이스**: `primavera_jpa_advanced`, `primavera_jpa_board`

### 📈 모니터링 확인
```bash
# Redis 연결 확인
redis-cli -p 6380 ping

# 캐시 상태 확인
redis-cli -p 6380 info stats

# Spring Boot Actuator
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

---

## 📋 Chapter 18: 마이크로서비스

### 🐳 Docker Compose 환경
```bash
# 기존 환경 종료
cd infrastructure
docker-compose -f docker-compose.jpa.yml down

# 마이크로서비스 전체 환경 시작 (시간 소요)
docker-compose -f docker-compose.microservices.yml up -d

# 모든 서비스 시작 대기 (약 2-3분)
docker-compose -f docker-compose.microservices.yml logs -f

# 상태 확인
docker-compose -f docker-compose.microservices.yml ps
```

### 🚀 메인 클래스 실행

#### 개별 마이크로서비스 실행
```bash
# User Service (사용자 서비스)
./gradlew :chap18:user-service:bootRun -Dspring.profiles.active=local -Dserver.port=8081

# Product Service (상품 서비스)
./gradlew :chap18:product-service:bootRun -Dspring.profiles.active=local -Dserver.port=8082

# Order Service (주문 서비스)
./gradlew :chap18:order-service:bootRun -Dspring.profiles.active=local -Dserver.port=8083

# Payment Service (결제 서비스)
./gradlew :chap18:payment-service:bootRun -Dspring.profiles.active=local -Dserver.port=8084

# Frontend Gateway (API 게이트웨이)
./gradlew :chap18:frontend-gateway:bootRun -Dspring.profiles.active=local -Dserver.port=8080
```

#### 통합 실행 (권장)
```bash
# 모든 마이크로서비스를 백그라운드에서 실행
./gradlew :chap18:bootRunAll -Dspring.profiles.active=local
```

### 📊 사용 가능한 서비스
- **MariaDB**: `localhost:3308`
- **Redis**: `localhost:6380`
- **Kafka**: `localhost:9092`
- **Elasticsearch**: `localhost:9200`
- **Kibana**: `localhost:5601`
- **API Gateway**: `localhost:8080`

### 🔍 마이크로서비스 상태 확인
```bash
# API Gateway Health Check
curl http://localhost:8080/actuator/health

# Service Registry 확인
curl http://localhost:8080/api/services

# Kafka Topics 확인
docker exec -it kafka-primavera-microservices kafka-topics.sh --bootstrap-server localhost:9092 --list

# Elasticsearch 확인
curl http://localhost:9200/_cluster/health
```

---

## 🛠️ 공통 유틸리티 명령어

### 🔄 환경 전환
```bash
# 현재 실행 중인 모든 Docker Compose 종료
docker-compose -f docker-compose.basic.yml down
docker-compose -f docker-compose.mybatis.yml down
docker-compose -f docker-compose.board.yml down
docker-compose -f docker-compose.jpa.yml down
docker-compose -f docker-compose.microservices.yml down

# 특정 환경만 시작
docker-compose -f docker-compose.{원하는환경}.yml up -d
```

### 🧹 완전 초기화
```bash
# 모든 컨테이너 및 볼륨 제거 (데이터 손실 주의!)
docker-compose -f docker-compose.basic.yml down -v
docker-compose -f docker-compose.mybatis.yml down -v
docker-compose -f docker-compose.board.yml down -v
docker-compose -f docker-compose.jpa.yml down -v
docker-compose -f docker-compose.microservices.yml down -v

# Docker 시스템 정리
docker system prune -f
```

### 📊 상태 모니터링
```bash
# 모든 컨테이너 상태
docker ps -a

# 리소스 사용량
docker stats

# 네트워크 확인
docker network ls

# 볼륨 확인
docker volume ls
```

---

## 🚨 문제 해결 가이드

### ❌ 포트 충돌 문제
```bash
# 포트 사용 중 확인
lsof -i :3308
lsof -i :8080

# 기존 프로세스 종료
kill -9 [PID]
```

### ❌ 데이터베이스 연결 실패
```bash
# MariaDB 로그 확인
docker-compose -f docker-compose.basic.yml logs mariadb

# 직접 데이터베이스 접속 테스트
docker exec -it mariadb-primavera-basic mariadb -u primavera -pprimavera -e "SHOW DATABASES;"
```

### ❌ Vault 연결 문제 (chap13)
```bash
# Vault 상태 확인
docker-compose -f docker-compose.board.yml logs vault

# Vault 토큰 재설정
export VAULT_TOKEN=primavera-dev-token
export VAULT_ADDR=http://localhost:8200
```

### ❌ 메모리 부족 (마이크로서비스)
```bash
# Docker 메모리 설정 확인
docker system df

# 불필요한 컨테이너 정리
docker system prune -a
```

---

## 📚 권장 실행 순서

### 🎓 학습자용 순서
1. **chap01-05**: 기본 개념 학습 (`docker-compose.basic.yml`)
2. **chap06-11**: 웹 개발 (`docker-compose.mybatis.yml`)
3. **chap12-13**: 고급 보안 (`docker-compose.board.yml`)
4. **chap14-17**: JPA 고급 (`docker-compose.jpa.yml`)
5. **chap18**: 마이크로서비스 (`docker-compose.microservices.yml`)

### ⚡ 빠른 테스트용
```bash
# 각 환경별 헬스체크
./scripts/health-check-all.sh

# 자동 테스트 실행
./gradlew test -Dspring.profiles.active=test
```

---

## 📞 도움이 필요한 경우

- **GitHub Issues**: [Primavera Issues](https://github.com/your-org/primavera/issues)
- **Documentation**: `infrastructure/sql/README-INIT-SQL.md`
- **Docker Compose 레퍼런스**: [Docker Compose Documentation](https://docs.docker.com/compose/)

---

> 💡 **팁**: 각 챕터 실행 전에 반드시 해당하는 Docker Compose 환경이 실행 중인지 확인하세요!
> 
> 🔥 **성능 최적화**: SSD 사용 시 Docker Desktop의 "Use gRPC FUSE for file sharing" 옵션을 활성화하면 성능이 향상됩니다.