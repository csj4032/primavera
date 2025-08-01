## chap11

### Test
* https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
* MockMvc
* WithUserDetails
* MockBean

### Thymeleaf
* https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html

### Pagination
* 페이지는 전체 아이템 수, 현재 페이지, 페이지 당 아이템, 페이지 노출 수를 이용

#### Paged
* 페이징을 위한 정보와 아이템을 관리

#### PageRequest
* 페이징을 위한 기본 정보를 관리

#### PageRequest
* 페이징을 위한 기본 정보를 관리

### wysihtml5 에디터 적용

## 🐳 인프라 설정

### Docker Compose 환경 설정

이 챕터는 **MyBatis + 보안 인프라**를 사용합니다:

```bash
# infrastructure 디렉터리로 이동
cd infrastructure

# MyBatis + 보안 학습용 Docker Compose 실행 (MariaDB)
docker-compose -f docker-compose.mybatis.yml up -d

# 서비스 상태 확인
docker-compose -f docker-compose.mybatis.yml ps

# 정리 (컨테이너 및 볼륨 삭제)
docker-compose -f docker-compose.mybatis.yml down -v
```

**포함된 서비스:**
- **MariaDB 11.4.7** (포트: 3308)
- MyBatis 전용 데이터베이스 스키마 자동 생성

**애플리케이션 실행:**
```bash
# 인프라 시작 후 애플리케이션 실행
./gradlew :chap11:bootRun -Dspring.profiles.active=local
```