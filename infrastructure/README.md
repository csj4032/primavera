# 🏗️ Primavera Infrastructure

Primavera 프로젝트의 **인프라스트럭처 설정 및 관리**를 위한 Docker Compose 기반 환경입니다.

[![MariaDB](https://img.shields.io/badge/MariaDB-11.4.7-brown.svg)](https://mariadb.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)

## 📋 목차

- [🎯 개요](#-개요)
- [🛠️ 사전 요구사항](#️-사전-요구사항)
- [🚀 빠른 시작](#-빠른-시작)
- [📊 데이터베이스 구조](#-데이터베이스-구조)
- [⚙️ 설정 정보](#️-설정-정보)
- [🔧 관리 명령어](#-관리-명령어)
- [🐛 트러블슈팅](#-트러블슈팅)
- [📈 모니터링](#-모니터링)
- [🔐 보안 설정](#-보안-설정)

---

## 🎯 개요

이 인프라스트럭처는 Primavera 프로젝트의 **모든 모듈이 공유하는 데이터베이스 환경**을 제공합니다.

### 🗂️ 구성 요소

```
infrastructure/
├── README.md              # 📖 이 문서
├── docker-compose.yml     # 🐳 Docker Compose 설정
├── init.sql              # 🗃️ 데이터베이스 초기화 스크립트
├── vault-init.sh         # 🔐 Vault 시크릿 초기화 스크립트
└── certs-init.sh         # 🔒 SSL 인증서 생성 스크립트
```

### 🎯 주요 특징

- **MariaDB 11.4.7**: 최신 안정 버전 사용
- **Redis 7**: 세션 저장소 및 캐싱 지원
- **6개 분리 데이터베이스**: 모듈별 목적에 따른 데이터베이스 분리
- **자동 초기화**: 컨테이너 시작 시 스키마 및 테스트 데이터 자동 생성
- **영구 저장**: Docker 볼륨을 통한 데이터 영속성 보장
- **UTF8MB4 지원**: 완전한 유니코드 문자 지원
- **HashiCorp Vault**: 민감정보 중앙집중식 보안 관리
- **SSL 인증서**: 개발용 HTTPS 지원을 위한 자체 서명 인증서

---

## 🛠️ 사전 요구사항

### 1. Docker & Docker Compose 설치

**macOS (Homebrew)**
```bash
# Docker Desktop 설치
brew install --cask docker

# Docker Desktop 실행 확인
docker --version
docker-compose --version
```

**Ubuntu/Debian**
```bash
# Docker 설치
sudo apt update
sudo apt install docker.io docker-compose

# 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER
newgrp docker
```

**Windows**
- [Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows/) 다운로드 및 설치

### 2. 시스템 요구사항

| 항목 | 최소 요구사항 | 권장 사양 |
|------|---------------|-----------|
| **메모리** | 2GB RAM | 4GB+ RAM |
| **디스크** | 2GB 여유 공간 | 5GB+ 여유 공간 |
| **포트** | 1109, 6379, 8200 포트 사용 가능 | - |

### 3. 포트 충돌 확인

```bash
# 포트 1109 (MariaDB) 사용 여부 확인
netstat -an | grep 1109
# 또는
lsof -i :1109

# 포트 6379 (Redis) 사용 여부 확인
netstat -an | grep 6379
# 또는
lsof -i :6379

# 포트 8200 (Vault) 사용 여부 확인
netstat -an | grep 8200
# 또는
lsof -i :8200

# 사용 중이면 해당 프로세스 종료
sudo kill -9 [PID]
```

---

## 🚀 빠른 시작

### 1. 프로젝트 클론 및 디렉토리 이동

```bash
# 프로젝트 클론 (이미 있다면 생략)
git clone https://github.com/csj4032/primavera.git
cd primavera/infrastructure
```

### 2. Docker Compose 실행

```bash
# 백그라운드에서 서비스 시작
docker-compose up -d

# 실행 상태 확인
docker-compose ps
```

**Docker 네트워크 자동 생성:**
- Docker Compose가 시작되면 `primavera-network`라는 브리지 네트워크가 자동으로 생성됩니다
- 모든 컨테이너는 이 네트워크를 통해 서로 통신할 수 있습니다
- 컨테이너 간 통신 시 컨테이너 이름을 호스트명으로 사용합니다 (예: `mariadb`, `vault`)

```bash
# 네트워크 확인
docker network ls | grep primavera

# 네트워크 상세 정보
docker network inspect infrastructure_primavera-network
```

**예상 출력:**
```
NAME                IMAGE               COMMAND                  SERVICE   CREATED         STATUS         PORTS
mariadb-primavera   mariadb:11.4.7      "docker-entrypoint.s…"   mariadb   2 minutes ago   Up 2 minutes   0.0.0.0:1109->3306/tcp
redis-primavera     redis:7-alpine      "docker-entrypoint.s…"   redis     2 minutes ago   Up 2 minutes   0.0.0.0:6379->6379/tcp
vault-primavera     hashicorp/vault:1.15 "docker-entrypoint.s…"   vault     2 minutes ago   Up 2 minutes   0.0.0.0:8200->8200/tcp
```

### 3. 데이터베이스 초기화 확인

```bash
# 컨테이너가 완전히 시작될 때까지 대기 (약 30초)
sleep 30

# 생성된 데이터베이스 목록 확인
docker exec mariadb-primavera mariadb -u root -proot -e "SHOW DATABASES;"
```

**예상 출력:**
```
Database
information_schema
mysql
performance_schema
primavera
primavera_basic
primavera_jpa_advanced
primavera_jpa_board
primavera_mybatis
primavera_mybatis_board
primavera_test
sys
```

### 4. 연결 테스트

```bash
# MariaDB 연결 테스트
docker exec mariadb-primavera mariadb -u primavera -pprimavera -e "SELECT 'Connection successful!' AS status;"

# Redis 연결 테스트
docker exec redis-primavera redis-cli ping

# Vault 상태 확인
curl -s http://localhost:8200/v1/sys/health | jq
```

### 5. Vault 시크릿 자동 초기화

Docker Compose 시작 시 Vault 초기화가 자동으로 실행됩니다:

```bash
# 백그라운드에서 서비스 시작 (Vault 자동 초기화 포함)
docker-compose up -d

# 초기화 로그 확인
docker-compose logs vault-init
```

**수동 초기화 (필요시):**
```bash
# Vault 초기화 스크립트 수동 실행
./vault-init.sh

# 또는 수동으로 기본 시크릿 설정
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='primavera-dev-token'

# 시크릿 엔진 활성화
vault secrets enable -path=secret kv-v2

# 프로젝트 시크릿 설정 예시 (환경별)
vault kv put secret/primavera/local \
  spring.datasource.url=jdbc:mariadb://localhost:1109/primavera \
  spring.datasource.username=primavera \
  spring.datasource.password=primavera
```

### 6. SSL 인증서 생성 (선택사항)

HTTPS를 사용하는 Chapter 10 등을 위한 인증서 생성:

```bash
# SSL 인증서 생성 스크립트 실행
./certs-init.sh

# /etc/hosts 파일에 도메인 추가 (macOS/Linux)
echo "127.0.0.1 local.primavera.com" | sudo tee -a /etc/hosts

# Windows의 경우 C:\Windows\System32\drivers\etc\hosts 파일에 추가:
# 127.0.0.1 local.primavera.com
```

---

## 📊 데이터베이스 구조

### 🗂️ 데이터베이스 분리 전략

각 데이터베이스는 **모듈별 목적**에 따라 분리되어 있습니다:

| 데이터베이스 | 대상 모듈 | 목적 | 테이블 수 |
|--------------|-----------|------|-----------|
| **`primavera`** | 기본 | 레거시 호환성 | 기본 스키마 |
| **`primavera_basic`** | chap03-05 | 기초 Spring Boot 학습 | 4개 |
| **`primavera_mybatis`** | chap06-07, chap11 | MyBatis 예제 및 고급 기능 | 16개 |
| **`primavera_jpa_advanced`** | chap14-16 | JPA 고급 매핑 및 관계 | 다수 |
| **`primavera_mybatis_board`** | chap12-13 | MyBatis 기반 계층형 게시판 | 9개 |
| **`primavera_jpa_board`** | chap17+ | JPA 기반 파일 처리 게시판 | 11개 |
| **`primavera_test`** | 전체 | TestContainers 통합 테스트 | 2개 |

### 📋 주요 테이블 구조

#### primavera_basic (기초 학습용)
```sql
USERS       -- 사용자 기본 정보
ROLES       -- 권한 관리
USER_ROLES  -- 사용자-권한 매핑
WINNERS     -- 예제 데이터 (스포츠 수상자)
```

#### primavera_mybatis (MyBatis 전용)
```sql
USERS, ROLES, USER_ROLES     -- 기본 사용자 관리
USER_CONNECTIONS             -- 소셜 로그인 연동
POSTS                        -- 게시글
ITEMS                        -- 상품 (Single Table 상속)
CONTACT_*                    -- 연락처 (Table Per Class 상속)
CANIDAE, FELIDAE, SCINCIDAE  -- 동물 분류 (Joined 상속)
PROFESSORS, STUDENTS         -- 교수-학생 관계
TABLE_SEQ                    -- 시퀀스 관리
```

#### primavera_jpa_board (고급 파일 처리)
```sql
USERS, ROLES, USER_ROLES     -- 사용자 관리
POSTS, COMMENTS              -- 게시판 시스템
FILE_UPLOADS                 -- 파일 업로드 관리
FILE_PROCESSING_RESULTS      -- 파일 처리 결과
FINANCIAL_DATA              -- Excel 파일 파싱 결과
KAKAOTALK_MESSAGES          -- CSV 채팅 분석 결과
SYSTEM_LOGS                 -- Sentry 연동 로그
```

### 🔗 모듈별 연결 정보

각 모듈에서는 다음과 같이 연결합니다:

```yaml
# application-local.yml (각 모듈)
spring:
  datasource:
    url: jdbc:mariadb://localhost:1109/[데이터베이스명]
    username: primavera
    password: primavera
    driver-class-name: org.mariadb.jdbc.Driver
```

**예시:**
- chap03: `jdbc:mariadb://localhost:1109/primavera_basic`
- chap12: `jdbc:mariadb://localhost:1109/primavera_mybatis_board`
- chap17: `jdbc:mariadb://localhost:1109/primavera_jpa_board`

---

## ⚙️ 설정 정보

### 🐳 Docker Compose 설정

```yaml
# docker-compose.yml
services:
  mariadb:
    image: mariadb:11.4.7
    container_name: mariadb-primavera
    restart: unless-stopped
    environment:
      MARIADB_ROOT_PASSWORD: root
      MARIADB_DATABASE: primavera
      MARIADB_USER: primavera
      MARIADB_PASSWORD: primavera
      MARIADB_AUTO_UPGRADE: 1
    ports:
      - "1109:3306"  # 호스트:컨테이너
    volumes:
      - mariadb_data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql:ro
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    networks:
      - primavera-network

  redis:
    image: redis:7-alpine
    container_name: redis-primavera
    restart: unless-stopped
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - primavera-network
    command: redis-server --appendonly yes --maxmemory 256mb --maxmemory-policy allkeys-lru

  vault:
    image: hashicorp/vault:1.15
    container_name: vault-primavera
    restart: unless-stopped
    cap_add:
      - IPC_LOCK
    environment:
      VAULT_DEV_ROOT_TOKEN_ID: primavera-dev-token
      VAULT_DEV_LISTEN_ADDRESS: 0.0.0.0:8200
    ports:
      - "8200:8200"
    volumes:
      - vault_data:/vault/data
    networks:
      - primavera-network

volumes:
  mariadb_data:
    driver: local
  redis_data:
    driver: local
  vault_data:
    driver: local

networks:
  primavera-network:
    driver: bridge  # 브리지 네트워크로 컨테이너 간 통신 지원
```

### 🌐 Docker 네트워크 설명

#### 네트워크 구성
- **네트워크 이름**: `primavera-network`
- **드라이버**: `bridge` (기본 Docker 브리지 네트워크)
- **실제 생성 이름**: `infrastructure_primavera-network` (프로젝트명_네트워크명)

#### 네트워크 특징
1. **자동 DNS 해석**: 컨테이너 이름으로 서로 통신 가능
   - MariaDB → Vault: `vault:8200`
   - Redis → MariaDB: `mariadb:3306`
   - Vault → Redis: `redis:6379`
   
2. **격리된 통신**: 같은 네트워크의 컨테이너끼리만 통신 가능

3. **Spring Boot 설정 예시**:
   ```yaml
   # 컨테이너 내부에서 실행될 때
   spring:
     datasource:
       url: jdbc:mariadb://mariadb:3306/primavera  # 'mariadb' 호스트명 사용
     redis:
       host: redis  # 'redis' 호스트명 사용
       port: 6379
   
   # 호스트에서 실행될 때
   spring:
     datasource:
       url: jdbc:mariadb://localhost:1109/primavera  # 포워딩된 포트 사용
     redis:
       host: localhost  # 'localhost' 사용
       port: 6379
   ```

#### 네트워크 관리 명령어
```bash
# 네트워크 목록 확인
docker network ls

# primavera 네트워크 상세 정보
docker network inspect infrastructure_primavera-network

# 네트워크에 연결된 컨테이너 확인
docker network inspect infrastructure_primavera-network | jq '.[0].Containers'

# 수동으로 네트워크 생성 (docker-compose가 자동으로 생성하므로 일반적으로 불필요)
docker network create --driver bridge primavera-network

# 네트워크 삭제 (주의: 연결된 컨테이너가 없어야 함)
docker network rm infrastructure_primavera-network
```

### 🔐 인증 정보

#### MariaDB
| 항목 | 값 | 용도 |
|------|----|----- |
| **Root 사용자** | `root` / `root` | 관리자 접근 |
| **애플리케이션 사용자** | `primavera` / `primavera` | 애플리케이션 연결 |
| **포트** | `1109` (외부) → `3306` (내부) | 데이터베이스 접근 |
| **기본 데이터베이스** | `primavera` | 레거시 호환 |

#### Redis
| 항목 | 값 | 용도 |
|------|----|----- |
| **포트** | `6379` | Redis 접근 |
| **메모리 제한** | `256MB` | 메모리 사용량 제한 |
| **데이터 지속성** | `appendonly yes` | AOF 로그 활성화 |
| **삭제 정책** | `allkeys-lru` | 메모리 부족 시 LRU 삭제 |

#### HashiCorp Vault
| 항목 | 값 | 용도 |
|------|----|----- |
| **개발 토큰** | `primavera-dev-token` | 개발 환경 접근 |
| **포트** | `8200` | Vault API/UI 접근 |
| **UI 접속** | http://localhost:8200 | 웹 인터페이스 |
| **시크릿 경로** | `secret/primavera/*` | 프로젝트 시크릿 |

### 📁 볼륨 설정

```bash
# MariaDB 볼륨 위치 확인
docker volume inspect infrastructure_mariadb_data

# MariaDB 볼륨 크기 확인
docker exec mariadb-primavera df -h /var/lib/mysql

# Redis 볼륨 위치 확인
docker volume inspect infrastructure_redis_data

# Redis 볼륨 크기 확인
docker exec redis-primavera df -h /data

# Vault 볼륨 위치 확인
docker volume inspect infrastructure_vault_data

# Vault 볼륨 크기 확인
docker exec vault-primavera df -h /vault/data
```

---

## 🔧 관리 명령어

### 📚 기본 조작

```bash
# === 서비스 관리 ===

# 서비스 시작
docker-compose up -d

# 서비스 중지
docker-compose stop

# 서비스 재시작
docker-compose restart

# 서비스 완전 종료 (컨테이너 삭제)
docker-compose down

# 서비스 + 볼륨 완전 삭제 (데이터 삭제됨!)
docker-compose down -v
```

### 🔍 상태 확인

```bash
# === 모니터링 ===

# 실행 중인 서비스 확인
docker-compose ps

# 실시간 로그 확인
docker-compose logs -f mariadb

# 최근 로그 100줄 확인
docker-compose logs --tail=100 mariadb

# 리소스 사용량 확인
docker stats mariadb-primavera redis-primavera vault-primavera

# MariaDB 컨테이너 내부 접속
docker exec -it mariadb-primavera /bin/bash

# Redis 컨테이너 내부 접속
docker exec -it redis-primavera /bin/sh

# Vault 컨테이너 내부 접속
docker exec -it vault-primavera /bin/sh
```

### 🗃️ 데이터베이스 관리

```bash
# === 데이터베이스 접근 ===

# Root 사용자로 접속
docker exec -it mariadb-primavera mariadb -u root -proot

# 애플리케이션 사용자로 접속
docker exec -it mariadb-primavera mariadb -u primavera -pprimavera

# 특정 데이터베이스에 직접 접속
docker exec -it mariadb-primavera mariadb -u primavera -pprimavera primavera_basic

# SQL 스크립트 실행
docker exec -i mariadb-primavera mariadb -u root -proot < init.sql

# 단일 쿼리 실행
docker exec mariadb-primavera mariadb -u root -proot -e "SHOW DATABASES;"

### 🔴 Redis 데이터 관리

```bash
# === Redis 데이터 접근 ===

# Redis CLI 접속
docker exec -it redis-primavera redis-cli

# 키 목록 확인
docker exec redis-primavera redis-cli KEYS "*"

# 특정 키 값 조회 (예: 세션 데이터)
docker exec redis-primavera redis-cli GET "session:*"

# 모든 키 삭제 (주의!)
docker exec redis-primavera redis-cli FLUSHALL

# Redis 메모리 사용량 확인
docker exec redis-primavera redis-cli INFO memory

# Redis 연결 수 확인
docker exec redis-primavera redis-cli INFO clients
```

### 🔐 Vault 시크릿 관리

#### 토큰 생성 및 저장

Docker Compose 실행 시 Vault 토큰이 자동으로 생성되어 `infrastructure/vault/` 폴더에 저장됩니다:

```bash
# 생성되는 토큰 파일들
infrastructure/vault/
├── app-token.txt     # 애플리케이션용 읽기 전용 토큰
├── dev-token.txt     # 개발자용 전체 권한 토큰
└── tokens.json       # 토큰 메타데이터 포함 JSON

# 토큰 사용 방법
export VAULT_TOKEN=$(cat infrastructure/vault/app-token.txt)
```

#### Vault CLI 사용

```bash
# === Vault 시크릿 접근 ===

# Vault CLI로 접속
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN=$(cat infrastructure/vault/app-token.txt)

# 시크릿 목록 조회
vault kv list secret/primavera

# 프로젝트 시크릿 조회 (환경별, 데이터베이스별)
vault kv get secret/primavera/common
vault kv get secret/primavera/local/basic
vault kv get secret/primavera/local/mybatis
vault kv get secret/primavera/local/mybatis-board
vault kv get secret/primavera/local/jpa-advanced
vault kv get secret/primavera/local/jpa-board
vault kv get secret/primavera/local/security

# 시크릿 저장/업데이트 (개발자 토큰 필요)
export VAULT_TOKEN=$(cat infrastructure/vault/dev-token.txt)
vault kv put secret/primavera/local/basic \
  spring.datasource.password=new-secure-password

# Vault UI 접속
# 브라우저에서 http://localhost:8200 접속
# 토큰: infrastructure/vault/app-token.txt 파일 내용 사용
```

#### 토큰 정책 및 권한

```bash
# 애플리케이션 토큰 (읽기 전용)
- 정책: primavera-app-read
- TTL: 720시간 (30일)
- 권한: secret/primavera/* 경로 읽기 전용

# 개발자 토큰 (전체 권한)
- 정책: primavera-dev-full
- TTL: 168시간 (7일)
- 권한: secret/primavera/* 경로 전체 권한
```

#### Spring Cloud Vault 설정 가이드

**📁 경로 구조 원리**

Spring Cloud Vault는 다음 순서로 Vault 경로를 탐색합니다:

1. `/secret/{application-name}/{profile}` - 앱별 + 프로필별 설정
2. `/secret/{application-name}` - 앱별 공통 설정  
3. `/secret/{default-context}/{profile}` - 공유 + 프로필별 설정
4. `/secret/{default-context}` - 전체 앱 공유 설정

**🔧 주요 설정 속성**

- **`application-name`**: 특정 애플리케이션만의 전용 설정 경로
  - 기본값: `spring.application.name` 속성값 사용
  - 예시: `primavera` → `/secret/primavera/*` 경로 탐색

- **`default-context`**: 여러 애플리케이션이 공유하는 공통 설정 경로
  - 기본값: `"application"`
  - 예시: `application` → `/secret/application/*` 경로 탐색

**🎯 Primavera 프로젝트 설정 예시**

```yaml
# application-local.yml
spring:
  cloud:
    vault:
      host: localhost
      port: 8200
      scheme: http
      authentication: TOKEN
      token: ${VAULT_TOKEN}
      kv:
        enabled: true
        backend: secret
        default-context: primavera
        application-name: primavera
  config:
    import: vault://secret/primavera/local/basic
```

**⚙️ 계층적 시크릿 구조**

```
secret/
├── primavera/                    # application-name 기반
│   ├── common                   # 모든 환경 공통 설정
│   ├── local/
│   │   ├── basic               # 로컬 환경 + 기본 모듈
│   │   ├── mybatis            # 로컬 환경 + MyBatis 모듈
│   │   └── security           # 로컬 환경 + 보안 설정
│   ├── test/                   # 테스트 환경 설정
│   └── prod/                   # 운영 환경 설정
```

**💡 권장 설정 패턴**

```yaml
# 명확한 경로 지정 (권장)
spring:
  config:
    import: vault://secret/primavera/local/basic
  cloud:
    vault:
      kv:
        default-context: ""  # 기본 탐색 비활성화
        application-name: primavera
```

이렇게 설정하면 정확히 원하는 시크릿 경로만 접근하여 성능과 보안을 모두 향상시킬 수 있습니다.

### 💾 백업 및 복원

```bash
# === 백업 ===

# 전체 데이터베이스 백업
docker exec mariadb-primavera mariadb-dump -u root -proot --all-databases > backup_all.sql

# 특정 데이터베이스 백업
docker exec mariadb-primavera mariadb-dump -u root -proot primavera_basic > backup_basic.sql

# 스키마만 백업 (데이터 제외)
docker exec mariadb-primavera mariadb-dump -u root -proot --no-data primavera_basic > schema_basic.sql

# === 복원 ===

# 백업 파일 복원
docker exec -i mariadb-primavera mariadb -u root -proot < backup_all.sql

# 특정 데이터베이스에 복원
docker exec -i mariadb-primavera mariadb -u root -proot primavera_basic < backup_basic.sql
```

### 🧹 정리 명령어

```bash
# === 시스템 정리 ===

# 중지된 컨테이너 정리
docker container prune

# 사용하지 않는 이미지 정리
docker image prune

# 사용하지 않는 볼륨 정리
docker volume prune

# 사용하지 않는 네트워크 정리
docker network prune

# 전체 시스템 정리 (주의!)
docker system prune -a
```

---

## 🐛 트러블슈팅

### 🚨 일반적인 문제들

#### 1. 컨테이너 시작 실패

**문제**: `docker-compose up -d` 실행 시 컨테이너가 시작되지 않음

```bash
# 문제 진단
docker-compose ps
docker-compose logs mariadb

# 해결 방법
# 1. 포트 충돌 확인
netstat -an | grep 1109
lsof -i :1109

# 2. 충돌하는 프로세스 종료
sudo kill -9 [PID]

# 3. 컨테이너 강제 재생성
docker-compose down
docker-compose up -d --force-recreate
```

#### 2. 연결 거부 (Connection refused)

**문제**: 애플리케이션에서 데이터베이스 연결 실패

```bash
# 문제 진단
# 1. 컨테이너 상태 확인
docker-compose ps

# 2. 포트 바인딩 확인
docker port mariadb-primavera

# 3. 네트워크 연결 테스트
telnet localhost 1109

# 해결 방법
# 1. 컨테이너가 완전히 시작될 때까지 대기
sleep 30

# 2. 컨테이너 재시작
docker-compose restart mariadb

# 3. 방화벽 확인 (Linux)
sudo ufw status
sudo ufw allow 1109
```

#### 3. 초기화 스크립트 미실행

**문제**: init.sql이 실행되지 않아 데이터베이스가 생성되지 않음

```bash
# 문제 진단
docker exec mariadb-primavera mariadb -u root -proot -e "SHOW DATABASES;"

# 해결 방법 (데이터 삭제됨 주의!)
# 1. 볼륨까지 완전 삭제
docker-compose down -v

# 2. 새로 시작 (초기화 스크립트 자동 실행)
docker-compose up -d

# 3. 초기화 완료 대기
sleep 60

# 4. 결과 확인
docker exec mariadb-primavera mariadb -u root -proot -e "SHOW DATABASES;"
```

#### 4. 인증 실패

**문제**: `Access denied for user` 오류

```bash
# 문제 진단
docker exec mariadb-primavera mariadb -u primavera -pprimavera -e "SELECT 1;"

# 해결 방법
# 1. 사용자 계정 재생성
docker exec -it mariadb-primavera mariadb -u root -proot
```

```sql
-- MariaDB 내에서 실행
DROP USER IF EXISTS 'primavera'@'%';
CREATE USER 'primavera'@'%' IDENTIFIED BY 'primavera';
GRANT ALL PRIVILEGES ON *.* TO 'primavera'@'%';
FLUSH PRIVILEGES;
EXIT;
```

#### 5. 메모리 부족

**문제**: 컨테이너가 OOM(Out of Memory)으로 종료됨

```bash
# 문제 진단
docker logs mariadb-primavera | grep -i "killed\|memory\|oom"

# 해결 방법
# 1. 메모리 제한 설정 추가 (docker-compose.yml)
```

```yaml
services:
  mariadb:
    # ... 기존 설정 ...
    deploy:
      resources:
        limits:
          memory: 2G
        reservations:
          memory: 512M
```

#### 6. 디스크 공간 부족

**문제**: 볼륨이 가득 참

```bash
# 문제 진단
docker exec mariadb-primavera df -h /var/lib/mysql
docker system df

# 해결 방법
# 1. 불필요한 로그 정리
docker exec mariadb-primavera mariadb -u root -proot -e "
  SET GLOBAL general_log = 'OFF';
  SET GLOBAL slow_query_log = 'OFF';
"

# 2. 바이너리 로그 정리
docker exec mariadb-primavera mariadb -u root -proot -e "PURGE BINARY LOGS BEFORE NOW();"

# 3. Docker 시스템 정리
docker system prune -f
```

### 🔧 고급 문제 해결

#### 1. 성능 최적화

```sql
-- MariaDB 내에서 실행하여 성능 개선
SET GLOBAL innodb_buffer_pool_size = 1073741824;  -- 1GB
SET GLOBAL query_cache_size = 134217728;          -- 128MB
SET GLOBAL max_connections = 200;
```

#### 2. 슬로우 쿼리 로깅

```bash
# docker-compose.yml에 추가
command: >
  --character-set-server=utf8mb4 
  --collation-server=utf8mb4_unicode_ci
  --slow-query-log=ON
  --slow-query-log-file=/var/log/mysql/slow.log
  --long-query-time=1
```

#### 3. 커넥션 풀 설정

```yaml
# 애플리케이션 설정 (application-local.yml)
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

## 📈 모니터링

### 📊 상태 확인 스크립트

```bash
#!/bin/bash
# health-check.sh
echo "=== Primavera Infrastructure Health Check ==="
echo

echo "1. 컨테이너 상태:"
docker-compose ps

echo -e "\n2. 데이터베이스 연결 테스트:"
docker exec mariadb-primavera mariadb -u primavera -pprimavera -e "SELECT 'OK' AS status;" 2>/dev/null && echo "✅ 연결 성공" || echo "❌ 연결 실패"

echo -e "\n3. 데이터베이스 목록:"
docker exec mariadb-primavera mariadb -u root -proot -e "SHOW DATABASES;" 2>/dev/null

echo -e "\n4. 리소스 사용량:"
docker stats mariadb-primavera redis-primavera vault-primavera --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"

echo -e "\n5. 포트 바인딩:"
docker port mariadb-primavera
docker port redis-primavera
docker port vault-primavera

echo -e "\n6. Vault 상태:"
curl -s http://localhost:8200/v1/sys/health | jq .
```

```bash
# 실행 권한 부여 후 실행
chmod +x health-check.sh
./health-check.sh
```

### 📋 성능 모니터링

```sql
-- 성능 관련 쿼리들
-- 1. 현재 연결 수 확인
SHOW STATUS LIKE 'Threads_connected';

-- 2. 쿼리 캐시 히트율 확인
SHOW STATUS LIKE 'Qcache%';

-- 3. InnoDB 버퍼 풀 상태
SHOW STATUS LIKE 'Innodb_buffer_pool%';

-- 4. 슬로우 쿼리 개수
SHOW STATUS LIKE 'Slow_queries';

-- 5. 실행 중인 프로세스 확인
SHOW PROCESSLIST;
```

---

## 🔐 보안 설정

### 🛡️ 기본 보안 조치

#### 1. 프로덕션 환경 설정

```yaml
# docker-compose.prod.yml (프로덕션용)
services:
  mariadb:
    image: mariadb:11.4.7
    container_name: mariadb-primavera-prod
    restart: always
    environment:
      MARIADB_ROOT_PASSWORD_FILE: /run/secrets/mariadb_root_password
      MARIADB_PASSWORD_FILE: /run/secrets/mariadb_password
      MARIADB_USER: primavera
      MARIADB_DATABASE: primavera
    secrets:
      - mariadb_root_password
      - mariadb_password
    ports:
      - "127.0.0.1:3306:3306"  # 로컬호스트만 접근 허용
    volumes:
      - mariadb_data:/var/lib/mysql
    command: >
      --character-set-server=utf8mb4 
      --collation-server=utf8mb4_unicode_ci
      --skip-networking=false
      --bind-address=127.0.0.1

secrets:
  mariadb_root_password:
    file: ./secrets/mariadb_root_password.txt
  mariadb_password:
    file: ./secrets/mariadb_password.txt
```

#### 2. 시크릿 파일 생성

```bash
# 시크릿 디렉토리 생성
mkdir -p secrets

# 강력한 패스워드 생성
openssl rand -base64 32 > secrets/mariadb_root_password.txt
openssl rand -base64 32 > secrets/mariadb_password.txt

# 권한 설정
chmod 600 secrets/*
```

#### 3. 불필요한 권한 제거

```sql
-- 프로덕션 환경에서 실행
-- 1. 테스트 데이터베이스 삭제
DROP DATABASE IF EXISTS test;

-- 2. 익명 사용자 삭제
DELETE FROM mysql.user WHERE User='';

-- 3. 원격 root 접속 차단
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');

-- 4. 권한 적용
FLUSH PRIVILEGES;
```

### 🔒 네트워크 보안

```bash
# 방화벽 설정 (Ubuntu/Debian)
sudo ufw allow from 172.16.0.0/12 to any port 1109 comment 'Docker network'
sudo ufw allow from 192.168.0.0/16 to any port 1109 comment 'Local network'
sudo ufw deny 1109 comment 'Deny all other access'
```

---

## 📞 지원 및 도움말

### 🆘 긴급 상황 대응

```bash
# === 긴급 복구 절차 ===

# 1. 서비스 완전 중지
docker-compose down

# 2. 백업 확인 (가능한 경우)
ls -la backup_*.sql

# 3. 데이터 볼륨 백업 (삭제 전 필수!)
docker run --rm -v infrastructure_mariadb_data:/data -v $(pwd):/backup alpine tar czf /backup/mariadb_data_backup.tar.gz -C /data .

# 4. 볼륨 초기화 및 재시작
docker-compose down -v
docker-compose up -d

# 5. 백업 복원 (필요시)
docker exec -i mariadb-primavera mariadb -u root -proot < backup_all.sql
```

### 📚 추가 자료

- **Docker Compose 문서**: https://docs.docker.com/compose/
- **MariaDB 11.4.7 문서**: https://mariadb.com/kb/en/changes-improvements-in-mariadb-1147/
- **HashiCorp Vault 문서**: https://developer.hashicorp.com/vault/docs
- **Primavera 프로젝트**: https://github.com/csj4032/primavera

### 🐛 이슈 리포트

문제 발생 시 다음 정보와 함께 이슈를 등록해주세요:

```bash
# 디버그 정보 수집
echo "=== Primavera Infrastructure Debug Info ==="
echo "Docker 버전: $(docker --version)"
echo "Docker Compose 버전: $(docker-compose --version)"
echo "운영체제: $(uname -a)"
echo
echo "=== 컨테이너 상태 ==="
docker-compose ps
echo
echo "=== 최근 로그 (마지막 50줄) ==="
docker-compose logs --tail=50 mariadb
```

---

<div align="center">

**🌸 Primavera 프로젝트의 인프라스트럭처입니다 🌸**

[⭐ GitHub에서 스타 주기](https://github.com/csj4032/primavera) | [📖 전체 문서 보기](https://github.com/csj4032/primavera/wiki) | [🐛 이슈 리포트](https://github.com/csj4032/primavera/issues)

**현재 상태**: ✅ MariaDB 11.4.7 | 🔴 Redis 7 | 🔐 HashiCorp Vault 1.15 | 🗃️ 6개 데이터베이스 | 🔒 보안 설정 완료

</div>