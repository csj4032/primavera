## chap07

### ApplicationConfiguration
* ApplicationConfiguration.java 어플리케이션 설정

### application.yml 어플리케이션 로그 설정
* application.yml LOGGING 에서 설정 가능
* [공식](https://logback.qos.ch/)
* [가이드](https://www.baeldung.com/logback)
```
logging:
  config: classpath:logback.xml
```

### Filter
```
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] .s.b.w.s.f.OrderedHiddenHttpMethodFilter : Filter 'hiddenHttpMethodFilter' configured for use
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] o.s.b.w.s.f.OrderedRequestContextFilter  : Filter 'requestContextFilter' configured for use
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] s.b.w.s.f.OrderedCharacterEncodingFilter : Filter 'characterEncodingFilter' configured for use
2019-04-16 19:14:35.999 DEBUG 48150 --- [  restartedMain] o.s.b.w.s.f.OrderedFormContentFilter     : Filter 'formContentFilter' configured for use
2019-04-16 19:14:36.001 DEBUG 48150 --- [  restartedMain] io.undertow                              : starting undertow server io.undertow.Undertow@b3f2d5b
``` 

### lucy-xss-filter
* [참고](https://github.com/naver/lucy-xss-filter)
