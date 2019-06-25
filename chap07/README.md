## chap07

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

### ETC
* Chain of Responsibility Pattern 참고