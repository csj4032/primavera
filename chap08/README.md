## chap08

### ApplicationConfiguration
* ApplicationConfiguration.java 어플리케이션 설정

### Filter
* org.springframework.boot.web.servlet.filter 패키지
```
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] .s.b.w.s.f.OrderedHiddenHttpMethodFilter : Filter 'hiddenHttpMethodFilter' configured for use
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] o.s.b.w.s.f.OrderedRequestContextFilter  : Filter 'requestContextFilter' configured for use
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] s.b.w.s.f.OrderedCharacterEncodingFilter : Filter 'characterEncodingFilter' configured for use
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] o.s.b.w.s.f.OrderedFormContentFilter     : Filter 'formContentFilter' configured for use
2019-04-16 19:14:36.001 DEBUG 48150 --- [  restartedMain] io.undertow                              : starting undertow server io.undertow.Undertow@b3f2d5b
``` 

### lucy-xss-filter
* [참고](https://github.com/naver/lucy-xss-filter)
```
@Bean
public FilterRegistrationBean<XssEscapeServletFilter> filterRegistrationBean() {
    FilterRegistrationBean<XssEscapeServletFilter> filterRegistration = new FilterRegistrationBean<>();
    filterRegistration.setFilter(new XssEscapeServletFilter());
    filterRegistration.setOrder(1);
    filterRegistration.addUrlPatterns("/*");
    return filterRegistration;
}
```

### Undertow
```
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-web') {
        exclude module: "spring-boot-starter-tomcat"
    }
    compile('org.springframework.boot:spring-boot-starter-undertow')
}
```

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
./gradlew :chap08:bootRun -Dspring.profiles.active=local
```

### ETC
* Chain of Responsibility Pattern 참고