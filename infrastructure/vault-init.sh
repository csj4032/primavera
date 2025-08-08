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
export VAULT_TOKEN='primavera-vault-token'

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

# KV v2 시크릿 엔진 활성화 (기본값)
echo -e "${YELLOW}2. KV v2 시크릿 엔진 확인...${NC}"
if vault secrets enable -path=secret kv-v2 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ KV v2 시크릿 엔진이 활성화되었습니다.${NC}"
else
    echo -e "${YELLOW}ℹ KV v2 시크릿 엔진이 이미 활성화되어 있습니다.${NC}"
fi
echo ""

# 챕터별 시크릿 저장 (메인 클래스 이름 기반)
echo -e "${YELLOW}3. 챕터별 Vault 시크릿 저장 (메인 클래스 이름 기반)...${NC}"

# Local 환경 설정 (챕터별 메인 클래스 이름 사용)
echo -e "${BLUE}   - Local 환경 시크릿 저장 중...${NC}"

# chap01: SpringBootStarterApplication
vault kv put secret/SpringBootStarterApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# chap02: ConfigurationDependencyApplication
vault kv put secret/ConfigurationDependencyApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# chap03: MvcAopApplication
vault kv put secret/MvcAopApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# chap04: DataAccessApplication
vault kv put secret/DataAccessApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# chap05: MyBatisLoggingApplication
vault kv put secret/MyBatisLoggingApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# chap06: ValidationApplication
vault kv put secret/ValidationApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
    lucy.xss.enabled=true \
    lucy.xss.rule-config-path=lucy-xss-servlet-filter-rule.xml

# chap07: ThymeleafWebApplication
vault kv put secret/ThymeleafWebApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# chap08: SecurityFilterApplication
vault kv put secret/SecurityFilterApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
    lucy.xss.enabled=true \
    lucy.xss.rule-config-path=lucy-xss-servlet-filter-rule.xml

# chap09: SpringSecurityBasicApplication
vault kv put secret/SpringSecurityBasicApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
    security.jwt.secret=primavera-local-jwt-secret-2024

# chap10: OAuth2SocialLoginApplication
vault kv put secret/OAuth2SocialLoginApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
    security.jwt.secret=primavera-local-jwt-secret-2024 \
    spring.security.oauth2.client.registration.google.client-id=google-client-id-local \
    spring.security.oauth2.client.registration.google.client-secret=google-client-secret-local \
    spring.security.oauth2.client.registration.facebook.client-id=facebook-client-id-local \
    spring.security.oauth2.client.registration.facebook.client-secret=facebook-client-secret-local \
    spring.security.oauth2.client.registration.github.client-id=github-client-id-local \
    spring.security.oauth2.client.registration.github.client-secret=github-client-secret-local \
    spring.security.oauth2.client.registration.kakao.client-id=kakao-client-id-local \
    spring.security.oauth2.client.registration.kakao.client-secret=kakao-client-secret-local

# chap11: BoardSystemApplication
vault kv put secret/BoardSystemApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# chap12: HierarchicalCommentApplication
vault kv put secret/HierarchicalCommentApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
    spring.flyway.enabled=true \
    spring.flyway.locations=classpath:db/migration

# chap13: AdvancedAuthorizationApplication
vault kv put secret/AdvancedAuthorizationApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
    spring.data.mongodb.uri=mongodb://localhost:27017/primavera_auth

# chap14: AdvancedJpaApplication
vault kv put secret/AdvancedJpaApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# chap15: JpaAdvancedMappingApplication
vault kv put secret/JpaAdvancedMappingApplication/local \
    spring.r2dbc.url=jdbc:mariadb://localhost:3306/primavera \
    spring.r2dbc.username=primavera \
    spring.r2dbc.password=primavera \
    spring.redis.host=localhost \
    spring.redis.port=6380

# chap16: FileProcessingMonitoringApplication
vault kv put secret/FileProcessingMonitoringApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
    sentry.dsn=https://your-sentry-dsn-local@sentry.io/project-id \
    aws.credentials.access-key=your-aws-access-key-id \
    aws.credentials.secret-key=your-aws-secret-access-key \
    aws.region=ap-northeast-2 \
    aws.s3.bucket-name=primavera-local-bucket \
    aws.s3.endpoint= \
    aws.s3.path-style-access=false

# chap17: FileProcessingMonitoringApplication (Data Pipeline)
vault kv put secret/FileProcessingMonitoringApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver \
    spring.elasticsearch.uris=http://localhost:9200 \
    spring.data.elasticsearch.client.reactive.username=elastic \
    spring.data.elasticsearch.client.reactive.password=changeme

# chap18: 마이크로서비스
vault kv put secret/FrontApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

vault kv put secret/AccountApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

vault kv put secret/ProductApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

vault kv put secret/ConfigurationApplication/local \
    spring.datasource.url=jdbc:mariadb://localhost:3306/primavera \
    spring.datasource.username=primavera \
    spring.datasource.password=primavera \
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver


echo -e "${GREEN}✓ 프로젝트 시크릿이 저장되었습니다.${NC}"
echo ""

# 정책 설정
echo -e "${YELLOW}4. Vault 접근 정책 설정...${NC}"

# 애플리케이션 읽기 전용 정책
vault policy write primavera-app-read - <<EOF
# 애플리케이션용 읽기 전용 정책 (챕터별 애플리케이션 이름 기반)
path "secret/data/SpringBootStarterApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/ConfigurationDependencyApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/MvcAopApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/DataAccessApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/MyBatisLoggingApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/ValidationApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/ThymeleafWebApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/SecurityFilterApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/SpringSecurityBasicApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/OAuth2SocialLoginApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/BoardSystemApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/HierarchicalCommentApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/AdvancedAuthorizationApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/AdvancedJpaApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/JpaAdvancedMappingApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/FileProcessingMonitoringApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/ProductBatchApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/ProductStreamingApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/AccountApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/ConfigurationApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/OrderApplication/*" {
  capabilities = ["read", "list"]
}
path "secret/data/ProductApplication/*" {
  capabilities = ["read", "list"]
}

# 메타데이터 접근 권한
path "secret/metadata/*" {
  capabilities = ["list", "read"]
}
EOF

# 개발자용 전체 권한 정책
vault policy write primavera-dev-full - <<EOF
# 개발자용 전체 권한 정책 (챕터별 애플리케이션 이름 기반)
path "secret/data/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
path "secret/metadata/*" {
  capabilities = ["list", "read", "delete"]
}
path "secret/destroy/*" {
  capabilities = ["update"]
}
path "secret/undelete/*" {
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

# 토큰을 파일에 저장
echo -e "${YELLOW}6. 토큰을 파일에 저장...${NC}"
VAULT_CONFIG_DIR="/vault/config"
mkdir -p $VAULT_CONFIG_DIR

# 애플리케이션 토큰 저장
echo "$APP_TOKEN" > $VAULT_CONFIG_DIR/app-token.txt
echo "$DEV_TOKEN" > $VAULT_CONFIG_DIR/dev-token.txt

# 토큰 정보를 JSON 형태로도 저장
cat > $VAULT_CONFIG_DIR/tokens.json <<EOF
{
  "app_token": {
    "token": "$APP_TOKEN",
    "policy": "primavera-app-read",
    "ttl": "720h",
    "description": "Application read-only token",
    "created_at": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  },
  "dev_token": {
    "token": "$DEV_TOKEN", 
    "policy": "primavera-dev-full",
    "ttl": "168h",
    "description": "Developer full access token",
    "created_at": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  }
}
EOF

# 권한 설정
chmod 600 $VAULT_CONFIG_DIR/app-token.txt
chmod 600 $VAULT_CONFIG_DIR/dev-token.txt
chmod 600 $VAULT_CONFIG_DIR/tokens.json

echo -e "${GREEN}✓ 토큰이 infrastructure/vault/ 디렉토리에 저장되었습니다.${NC}"
echo -e "  - app-token.txt: 애플리케이션용 토큰"
echo -e "  - dev-token.txt: 개발자용 토큰"
echo -e "  - tokens.json: 전체 토큰 정보"
echo ""

# 결과 출력
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    Vault 초기화 완료! (챕터별 구성)${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${YELLOW}생성된 토큰 정보:${NC}"
echo -e "  ${GREEN}애플리케이션 토큰 (읽기 전용):${NC}"
echo -e "  $APP_TOKEN"
echo ""
echo -e "  ${GREEN}개발자 토큰 (전체 권한):${NC}"
echo -e "  $DEV_TOKEN"
echo ""
echo -e "${YELLOW}챕터별 Vault 경로 구조:${NC}"
echo -e "  ${BLUE}secret/{ApplicationName}/{environment}${NC}"
echo -e "  예시:"
echo -e "  - secret/DataAccessApplication/local"
echo -e "  - secret/DataAccessApplication/test"
echo ""
echo -e "${YELLOW}사용 방법:${NC}"
echo -e "  1. 애플리케이션 설정에 토큰 추가:"
echo -e "     ${BLUE}export VAULT_TOKEN=$APP_TOKEN${NC}"
echo ""
echo -e "  2. 시크릿 조회 예시:"
echo -e "     ${BLUE}vault kv get secret/DataAccessApplication/local${NC}"
echo -e "     ${BLUE}vault kv get secret/OAuth2SocialLoginApplication/local${NC}"
echo -e "     ${BLUE}vault kv get secret/HierarchicalCommentApplication/local${NC}"
echo ""
echo -e "  3. Spring Boot 실행 예시 (application-local.yml 설정 기반):"
echo -e "     ${BLUE}# chap04 (DataAccessApplication)${NC}"
echo -e "     ${BLUE}export VAULT_TOKEN=$APP_TOKEN${NC}"
echo -e "     ${BLUE}./gradlew :chap04:bootRun -Dspring.profiles.active=local${NC}"
echo ""
echo -e "     ${BLUE}# chap10 (OAuth2SocialLoginApplication)${NC}"
echo -e "     ${BLUE}export VAULT_TOKEN=$APP_TOKEN${NC}"
echo -e "     ${BLUE}./gradlew :chap10:bootRun -Dspring.profiles.active=local${NC}"
echo ""
echo -e "     ${BLUE}# chap12 (HierarchicalCommentApplication)${NC}"
echo -e "     ${BLUE}export VAULT_TOKEN=$APP_TOKEN${NC}"
echo -e "     ${BLUE}./gradlew :chap12:bootRun -Dspring.profiles.active=local${NC}"
echo ""
echo -e "${YELLOW}장점:${NC}"
echo -e "  - 각 챕터가 고유한 네임스페이스를 가짐"
echo -e "  - 메인 클래스 이름으로 명확한 식별"
echo -e "  - 환경별 설정 분리 (local/test)"
echo -e "  - Vault 정책으로 세밀한 접근 제어"
echo ""
echo -e "${YELLOW}주의사항:${NC}"
echo -e "  - 개발/테스트 환경 전용 설정입니다."
echo -e "  - 프로덕션 환경은 별도로 구축해야 합니다."
echo ""