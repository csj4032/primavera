#!/bin/bash

# Primavera 프로젝트용 SSL 인증서 생성 스크립트

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   Primavera SSL 인증서 생성 스크립트${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 작업 디렉토리 설정
WORK_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CERTS_DIR="${WORK_DIR}/certs"

# 디렉토리 생성
mkdir -p "${CERTS_DIR}"

echo -e "${YELLOW}1. 기존 인증서 파일 정리...${NC}"
# 기존 파일 정리
rm -f "${CERTS_DIR}"/*.crt "${CERTS_DIR}"/*.csr "${CERTS_DIR}"/*.key "${CERTS_DIR}"/*.pem "${CERTS_DIR}"/*.der "${CERTS_DIR}"/*.p12 "${CERTS_DIR}"/extfile

echo -e "${YELLOW}2. CA(Certificate Authority) 인증서 생성...${NC}"
# CA 키 및 인증서 생성
openssl req -new -x509 \
    -keyout "${CERTS_DIR}/primavera-ca.key" \
    -out "${CERTS_DIR}/primavera-ca.crt" \
    -days 3650 \
    -subj '/CN=Primavera CA/OU=Development/O=Primavera/L=Seoul/S=Seoul/C=KR' \
    -passin pass:primavera \
    -passout pass:primavera

echo -e "${GREEN}✓ CA 인증서 생성 완료${NC}"

echo -e "${YELLOW}3. 서버 인증서 키 생성...${NC}"
# 서버 개인키 생성 (비밀번호 없음)
openssl genrsa -out "${CERTS_DIR}/primavera.key" 2048

echo -e "${YELLOW}4. 서버 인증서 요청(CSR) 생성...${NC}"
# 서버 인증서 요청 생성
openssl req -new \
    -key "${CERTS_DIR}/primavera.key" \
    -out "${CERTS_DIR}/primavera.csr" \
    -subj '/CN=local.primavera.com/OU=Development/O=Primavera/L=Seoul/S=Seoul/C=KR'

echo -e "${YELLOW}5. SAN(Subject Alternative Names) 확장 설정...${NC}"
# SAN 확장을 위한 설정 파일 생성
cat > "${CERTS_DIR}/extfile" << EOF
[req]
distinguished_name = req_distinguished_name
x509_extensions = v3_req
prompt = no

[req_distinguished_name]
CN = local.primavera.com

[v3_req]
subjectAltName = @alt_names
keyUsage = keyEncipherment, dataEncipherment
extendedKeyUsage = serverAuth

[alt_names]
DNS.1 = local.primavera.com
DNS.2 = localhost
DNS.3 = 127.0.0.1
DNS.4 = primavera.local
IP.1 = 127.0.0.1
IP.2 = ::1
EOF

echo -e "${YELLOW}6. CA로 서버 인증서 서명...${NC}"
# CA로 서버 인증서 서명
openssl x509 -req \
    -CA "${CERTS_DIR}/primavera-ca.crt" \
    -CAkey "${CERTS_DIR}/primavera-ca.key" \
    -in "${CERTS_DIR}/primavera.csr" \
    -out "${CERTS_DIR}/primavera.crt" \
    -days 365 \
    -CAcreateserial \
    -passin pass:primavera \
    -extensions v3_req \
    -extfile "${CERTS_DIR}/extfile"

echo -e "${GREEN}✓ 서버 인증서 생성 완료${NC}"

echo -e "${YELLOW}7. PKCS12 키스토어 생성...${NC}"
# PKCS12 키스토어 생성 (Spring Boot에서 사용)
openssl pkcs12 -export \
    -in "${CERTS_DIR}/primavera.crt" \
    -inkey "${CERTS_DIR}/primavera.key" \
    -out "${CERTS_DIR}/primavera.p12" \
    -name "primavera" \
    -CAfile "${CERTS_DIR}/primavera-ca.crt" \
    -caname "Primavera CA" \
    -passout pass:primavera

echo -e "${GREEN}✓ PKCS12 키스토어 생성 완료${NC}"

echo -e "${YELLOW}8. 인증서 정보 검증...${NC}"
# 생성된 인증서 정보 확인
echo -e "${BLUE}📋 생성된 인증서 정보:${NC}"
openssl x509 -in "${CERTS_DIR}/primavera.crt" -text -noout | grep -E "(Subject:|DNS:|IP Address:|Not Before|Not After)"

echo -e "${BLUE}📋 PKCS12 키스토어 정보:${NC}"
keytool -list -keystore "${CERTS_DIR}/primavera.p12" -storepass primavera -storetype PKCS12

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}        SSL 인증서 생성 완료!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "${YELLOW}생성된 파일:${NC}"
echo -e "  ${GREEN}CA 인증서:${NC} ${CERTS_DIR}/primavera-ca.crt"
echo -e "  ${GREEN}서버 인증서:${NC} ${CERTS_DIR}/primavera.crt"
echo -e "  ${GREEN}서버 개인키:${NC} ${CERTS_DIR}/primavera.key"
echo -e "  ${GREEN}PKCS12 키스토어:${NC} ${CERTS_DIR}/primavera.p12"
echo ""
echo -e "${YELLOW}인증서 사용 방법:${NC}"
echo -e "  1. 필요한 챕터의 resources 폴더에 인증서 복사:"
echo -e "     ${BLUE}cp ${CERTS_DIR}/primavera.p12 /path/to/chapXX/src/main/resources/${NC}"
echo ""
echo -e "${YELLOW}Spring Boot 설정 (application.yml):${NC}"
echo -e "${BLUE}server:${NC}"
echo -e "${BLUE}  ssl:${NC}"
echo -e "${BLUE}    key-store: classpath:primavera.p12${NC}"
echo -e "${BLUE}    key-store-password: primavera${NC}"
echo -e "${BLUE}    key-store-type: PKCS12${NC}"
echo -e "${BLUE}    key-alias: primavera${NC}"
echo -e "${BLUE}    enabled: true${NC}"
echo -e "${BLUE}  port: 8443${NC}"
echo ""
echo -e "${YELLOW}/etc/hosts 설정 추가:${NC}"
echo -e "${BLUE}127.0.0.1 local.primavera.com${NC}"
echo ""
echo -e "${YELLOW}브라우저 접속:${NC}"
echo -e "${BLUE}https://local.primavera.com:8443${NC}"
echo ""
echo -e "${YELLOW}주의사항:${NC}"
echo -e "- 개발용 자체 서명 인증서입니다."
echo -e "- 브라우저에서 보안 경고가 표시될 수 있습니다."
echo -e "- 프로덕션 환경에서는 신뢰할 수 있는 CA에서 발급받은 인증서를 사용하세요."
echo ""