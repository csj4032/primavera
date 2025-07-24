# Primavera: 실전으로 배우는 Spring Boot 커뮤니티 개발

---

## 서문

이 책은 ���무에서 바로 활용할 수 있는 Spring Boot 기반 커뮤니티 사이트 개발 과정을 단계별로 안내합니다. 각 챕터는 실제 프로젝트 구조와 코드를 바탕으로, 실습과 이론을 균형 있게 다룹니다. Spring Boot의 핵심 개념부터 보안, 소셜 로그인, CI/CD, 클라우드 배포까지, 실전에서 꼭 필요한 내용을 담았습니다.

---

## 목차

1. **Spring Boot 시작과 프로젝트 구조 이해**
2. **빌드 도구와 환경 설정**
3. **Spring Boot 핵심 어노테이션과 설정**
4. **테스트와 AOP**
5. **데이터 접근과 트랜잭션**
6. **MyBatis와 동적 쿼리**
7. **입력 검증과 비동기 처리**
8. **Thymeleaf와 인증**
9. **필터, 로깅, Undertow**
10. **디자인 패턴과 보안**
11. **소셜 로그인과 HTTPS**
12. **게시판 구현**
13. **계층형 댓글 시스템**
14. **권한 관리와 파일 업로드**
15. **JPA와 외부 API 연동**
16. **리액티브 프로그래밍과 마이크로서비스**
17. **고급 기능(캐싱, 스케줄링, ���벤트)**
18. **배포, 모니터링, CI/CD**
19. **환경별/도메인별 Config 관리**
20. **부록: 실전 팁 & 참고 자료**

---

## 1장. Spring Boot 시작과 프로젝트 구조 이해
- Spring Boot란 무엇인가?
- 프로젝트 구조 및 모듈 설명
- 실습: 프로젝트 클론 및 빌드

## 2장. 빌드 도구와 환경 설정
- Gradle/Maven 비교
- gradle.properties, build.gradle 구조
- 멀티모듈 구성

## 3장. Spring Boot 핵심 어노테이션과 설정
- @SpringBootConfiguration, @EnableAutoConfiguration, SpringApplicationBuilder 설명
- ApplicationContext와 빈 라이프사이클

## 4장. 테스트와 AOP
- 단위/통합 테스트
- @SpringBootTest, MockMvc
- AOP 개념과 실습

## 5장. 데이터 접근과 트랜잭션
- DataSource, JdbcTemplate
- 커넥션 풀, 트랜잭션 관리

## 6장. MyBatis와 동적 쿼리
- MyBatis 매퍼, 동적 SQL
- Logback, SQL 로깅

## 7장. 입력 검증과 비동기 처리
- Bean Validation, 사용자 정의 검증
- AJAX, 함수형 프로그래밍

## 8장. Thymeleaf와 인증
- 템플릿 엔진, 레이아웃
- 로그인/세션 관리

## 9장. 필터, 로깅, Undertow
- 웹/보안 필터, Lucy-xss-filter
- Log4jdbc, Undertow 서버

## 10장. 디자인 패턴과 보안
- 책임 연쇄 패턴
- Spring Security, 커스텀 스타터

## 11장. 소셜 로그인과 HTTPS
- PKCS12, 자체 서명 인증서
- OAuth2(Google, Facebook, Kakao)
- 역할 기반 접근 제어

## 12장. 게시판 구현
- CRUD, 권한별 접근
- 페이징, 리치 텍스트 에디터

## 13장. 계층형 댓글 시스템
- 트리 구조, 대댓글
- 정렬 알고리즘

## 14장. 권한 관리와 파일 업로드
- @PreAuthorize, @Secured
- 파일 업로드/다운로드, 이미지 리사이징

## 15장. JPA와 외부 API 연동
- 엔티티 매핑, JPQL
- ModelMapper, 카카오 API

## 16장. 리액티브 프로그래밍과 마이크로서비스
- WebFlux, Mono/Flux
- Config Server, 서비스 분리

## 17장. 고급 기능(캐싱, 스케줄링, 이벤트)
- Spring Cache, Redis
- @Scheduled, Quartz
- Spring Events

## 18장. 배포, 모니터링, CI/CD
- Docker, 멀티스테이지 빌드
- Travis CI, 커버리지
- Actuator, Prometheus, Grafana

## 19장. 환경별/도메인별 Config 관리
- config/ 디렉토리 구조
- 환경별/도메인별 분리의 필요성
- 예시 및 Best Practice

## 20장. 부록: 실전 팁 & 참고 자료
- 실무에서 자주 쓰는 명령어
- 참고 사이트, 공식 문서

---

## 맺음말

이 책은 Spring Boot 기반의 실전 프로젝트를 통해, 백엔드 개발의 전 과정을 경험할 수 있도록 구성되었습니다. 각 챕터의 실습과 설명을 따라가며, 자신만의 커뮤니티 서비스를 완성해보세요. 여러분의 성장과 도전을 응원합니다!

---

## 부록

- **프로젝트 전체 빌드 명령어**: `./gradlew clean build`
- **Travis CI 사용법**: 저장소에 .travis.yml 추가 → Travis CI 사이트에서 활성화 → 커밋 시 자동 빌드
- **라이선스**: 누구나 자유롭게 사용, 출처 표기 불필요
- **참고 자료**: Spring 공식 문서, Baeldung, Stack Overflow 등

---

(자세한 예제와 실습 코드는 각 챕터별 README.md 및 소스코드를 참고하세요.)
