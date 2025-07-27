#!/bin/bash

# Vault 초기화 스크립트
# HashiCorp Vault에 Primavera 프로젝트의 기본 시크릿을 설정합니다.

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Vault 설정
export VAULT_ADDR='http://localhost:8200'
export VAULT_TOKEN='primavera-dev-token'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Primavera Vault 초기화 스크립트${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Vault 상태 확인
echo -e "${YELLOW}1. Vault 서버 상태 확인...${NC}"
if ! vault status > /dev/null 2>&1; then
    echo -e "${RED}❌ Vault 서버가 실행되지 않습니다.${NC}"
    echo -e "${YELLOW}   docker-compose up -d vault 명령을 먼저 실행하세요.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Vault 서버가 정상적으로 실행 중입니다.${NC}"
echo ""

# KV v2 시크릿 엔진 활성화
echo -e "${YELLOW}2. KV v2 시크릿 엔진 활성화...${NC}"
if vault secrets enable -path=secret kv-2 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ KV v2 시크릿 엔진이 활성화되었습니다.${NC}"
else
    echo -e "${YELLOW}ℹ KV v2 시크릿 엔진이 이미 활성화되어 있습니다.${NC}"
fi
echo ""

# 프로젝트 전체 시크릿 저장
echo -e "${YELLOW}3. Primavera 프로젝트 시크릿 저장...${NC}"

# 공통 설정
vault kv put secret/primavera/common \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
    security.jwt.expiration=86400 \
    security.encryption.algorithm=AES

# Local 환경 설정 (데이터베이스별)
echo -e "${BLUE}   - Local 환경 시크릿 저장 중...${NC}"

# 기본 데이터베이스
vault kv put secret/primavera/local/default \
    spring.datasource.url=jdbc:mariadb://localhost:1109/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera

# 기본 예제 (chap03-chap05)
vault kv put secret/primavera/local/basic \
    spring.datasource.url=jdbc:mariadb://localhost:1109/primavera_basic \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera

# MyBatis 예제 (chap06-chap11)
vault kv put secret/primavera/local/mybatis \
    spring.datasource.url=jdbc:mariadb://localhost:1109/primavera_mybatis \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera

# MyBatis 게시판 (chap12-chap13)
vault kv put secret/primavera/local/mybatis-board \
    spring.datasource.url=jdbc:mariadb://localhost:1109/primavera_mybatis_board \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera

# JPA 고급 (chap14-chap16)
vault kv put secret/primavera/local/jpa-advanced \
    spring.datasource.url=jdbc:mariadb://localhost:1109/primavera_jpa_advanced \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera

# JPA 게시판 (chap17+)
vault kv put secret/primavera/local/jpa-board \
    spring.datasource.url=jdbc:mariadb://localhost:1109/primavera_jpa_board \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera

# 테스트용
vault kv put secret/primavera/local/test \
    spring.datasource.url=jdbc:mariadb://localhost:1109/primavera_test \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera

# 공통 보안 설정
vault kv put secret/primavera/local/security \
    security.jwt.secret=primavera-local-jwt-secret-2024 \
    oauth2.google.client-id=google-client-id-local \
    oauth2.google.client-secret=google-client-secret-local \
    oauth2.kakao.client-id=kakao-client-id-local \
    oauth2.kakao.client-secret=kakao-client-secret-local \
    oauth2.naver.client-id=naver-client-id-local \
    oauth2.naver.client-secret=naver-client-secret-local

# Test 환경 설정 (데이터베이스별)
echo -e "${BLUE}   - Test 환경 시크릿 저장 중...${NC}"

# 기본 데이터베이스
vault kv put secret/primavera/test/default \
    spring.datasource.url=jdbc:mariadb://mariadb:3306/primavera \
    spring.datasource.username=test_user \
    spring.datasource.password=test_password

# 기본 예제 (chap03-chap05)
vault kv put secret/primavera/test/basic \
    spring.datasource.url=jdbc:mariadb://mariadb:3306/primavera_basic \
    spring.datasource.username=test_user \
    spring.datasource.password=test_password

# MyBatis 예제 (chap06-chap11)
vault kv put secret/primavera/test/mybatis \
    spring.datasource.url=jdbc:mariadb://mariadb:3306/primavera_mybatis \
    spring.datasource.username=test_user \
    spring.datasource.password=test_password

# MyBatis 게시판 (chap12-chap13)
vault kv put secret/primavera/test/mybatis-board \
    spring.datasource.url=jdbc:mariadb://mariadb:3306/primavera_mybatis_board \
    spring.datasource.username=test_user \
    spring.datasource.password=test_password

# JPA 고급 (chap14-chap16)
vault kv put secret/primavera/test/jpa-advanced \
    spring.datasource.url=jdbc:mariadb://mariadb:3306/primavera_jpa_advanced \
    spring.datasource.username=test_user \
    spring.datasource.password=test_password

# JPA 게시판 (chap17+)
vault kv put secret/primavera/test/jpa-board \
    spring.datasource.url=jdbc:mariadb://mariadb:3306/primavera_jpa_board \
    spring.datasource.username=test_user \
    spring.datasource.password=test_password

# 테스트용
vault kv put secret/primavera/test/test \
    spring.datasource.url=jdbc:mariadb://mariadb:3306/primavera_test \
    spring.datasource.username=test_user \
    spring.datasource.password=test_password

# 공통 보안 설정
vault kv put secret/primavera/test/security \
    security.jwt.secret=primavera-test-jwt-secret-2024 \
    oauth2.google.client-id=google-client-id-test \
    oauth2.google.client-secret=google-client-secret-test \
    oauth2.kakao.client-id=kakao-client-id-test \
    oauth2.kakao.client-secret=kakao-client-secret-test \
    oauth2.naver.client-id=naver-client-id-test \
    oauth2.naver.client-secret=naver-client-secret-test

# Prod 환경 설정 (데이터베이스별)
echo -e "${BLUE}   - Prod 환경 시크릿 저장 중...${NC}"

# 기본 데이터베이스
vault kv put secret/primavera/prod/default \
    spring.datasource.url=jdbc:mariadb://prod-db-server:3306/primavera_prod \
    spring.datasource.username=prod_user \
    spring.datasource.password=prod_secure_password_change_me

# 기본 예제 (chap03-chap05)
vault kv put secret/primavera/prod/basic \
    spring.datasource.url=jdbc:mariadb://prod-db-server:3306/primavera_basic_prod \
    spring.datasource.username=prod_user \
    spring.datasource.password=prod_secure_password_change_me

# MyBatis 예제 (chap06-chap11)
vault kv put secret/primavera/prod/mybatis \
    spring.datasource.url=jdbc:mariadb://prod-db-server:3306/primavera_mybatis_prod \
    spring.datasource.username=prod_user \
    spring.datasource.password=prod_secure_password_change_me

# MyBatis 게시판 (chap12-chap13)
vault kv put secret/primavera/prod/mybatis-board \
    spring.datasource.url=jdbc:mariadb://prod-db-server:3306/primavera_mybatis_board_prod \
    spring.datasource.username=prod_user \
    spring.datasource.password=prod_secure_password_change_me

# JPA 고급 (chap14-chap16)
vault kv put secret/primavera/prod/jpa-advanced \
    spring.datasource.url=jdbc:mariadb://prod-db-server:3306/primavera_jpa_advanced_prod \
    spring.datasource.username=prod_user \
    spring.datasource.password=prod_secure_password_change_me

# JPA 게시판 (chap17+)
vault kv put secret/primavera/prod/jpa-board \
    spring.datasource.url=jdbc:mariadb://prod-db-server:3306/primavera_jpa_board_prod \
    spring.datasource.username=prod_user \
    spring.datasource.password=prod_secure_password_change_me

# 테스트용
vault kv put secret/primavera/prod/test \
    spring.datasource.url=jdbc:mariadb://prod-db-server:3306/primavera_test_prod \
    spring.datasource.username=prod_user \
    spring.datasource.password=prod_secure_password_change_me

# 공통 보안 설정
vault kv put secret/primavera/prod/security \
    security.jwt.secret=primavera-prod-jwt-secret-2024-change-me \
    oauth2.google.client-id=google-client-id-production \
    oauth2.google.client-secret=google-client-secret-production \
    oauth2.kakao.client-id=kakao-client-id-production \
    oauth2.kakao.client-secret=kakao-client-secret-production \
    oauth2.naver.client-id=naver-client-id-production \
    oauth2.naver.client-secret=naver-client-secret-production

echo -e "${GREEN}✓ 프로젝트 시크릿이 저장되었습니다.${NC}"
echo ""

# 정책 설정
echo -e "${YELLOW}4. Vault 접근 정책 설정...${NC}"

# 애플리케이션 읽기 전용 정책
vault policy write primavera-app-read - <<EOF
# 애플리케이션용 읽기 전용 정책
path "secret/data/primavera/*" {
  capabilities = ["read", "list"]
}
path "secret/metadata/primavera/*" {
  capabilities = ["list", "read"]
}
EOF

# 개발자용 전체 권한 정책
vault policy write primavera-dev-full - <<EOF
# 개발자용 전체 권한 정책
path "secret/data/primavera/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
path "secret/metadata/primavera/*" {
  capabilities = ["list", "read", "delete"]
}
path "secret/destroy/primavera/*" {
  capabilities = ["update"]
}
path "secret/undelete/primavera/*" {
  capabilities = ["update"]
}
EOF

echo -e "${GREEN}✓ 접근 정책이 설정되었습니다.${NC}"
echo ""

# 토큰 생성
echo -e "${YELLOW}5. 애플리케이션 토큰 생성...${NC}"
APP_TOKEN=$(vault token create -policy="primavera-app-read" -ttl=720h -format=json | jq -r '.auth.client_token')
DEV_TOKEN=$(vault token create -policy="primavera-dev-full" -ttl=168h -format=json | jq -r '.auth.client_token')

echo -e "${GREEN}✓ 토큰이 생성되었습니다.${NC}"
echo ""

# 결과 출력
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}        Vault 초기화 완료!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${YELLOW}생성된 토큰 정보:${NC}"
echo -e "  ${GREEN}애플리케이션 토큰 (읽기 전용):${NC}"
echo -e "  $APP_TOKEN"
echo ""
echo -e "  ${GREEN}개발자 토큰 (전체 권한):${NC}"
echo -e "  $DEV_TOKEN"
echo ""
echo -e "${YELLOW}사용 방법:${NC}"
echo -e "  1. 애플리케이션 설정에 토큰 추가:"
echo -e "     ${BLUE}export VAULT_TOKEN=$APP_TOKEN${NC}"
echo ""
echo -e "  2. 시크릿 조회 예시:"
echo -e "     ${BLUE}vault kv get secret/primavera/common${NC}"
echo -e "     ${BLUE}vault kv get secret/primavera/local/basic${NC}"
echo -e "     ${BLUE}vault kv get secret/primavera/local/mybatis${NC}"
echo -e "     ${BLUE}vault kv get secret/primavera/local/security${NC}"
echo ""
echo -e "  3. Spring Boot 실행 예시:"
echo -e "     ${BLUE}# chap03-05 (basic)${NC}"
echo -e "     ${BLUE}./gradlew :chap04:bootRun -Dspring.profiles.active=vault,local -Dvault.path=primavera/local/basic${NC}"
echo ""
echo -e "     ${BLUE}# chap06-11 (mybatis)${NC}"
echo -e "     ${BLUE}./gradlew :chap07:bootRun -Dspring.profiles.active=vault,local -Dvault.path=primavera/local/mybatis${NC}"
echo ""
echo -e "     ${BLUE}# chap12-13 (mybatis-board)${NC}"
echo -e "     ${BLUE}./gradlew :chap12:bootRun -Dspring.profiles.active=vault,local -Dvault.path=primavera/local/mybatis-board${NC}"
echo ""
echo -e "${YELLOW}주의사항:${NC}"
echo -e "  - 개발 환경 전용 설정입니다."
echo -e "  - 프로덕션에서는 반드시 TLS를 활성화하고"
echo -e "  - 보안이 강화된 인증 방법을 사용하세요."
echo ""