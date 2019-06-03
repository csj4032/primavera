## chap12

### Application.yml 언제 어떻게 읽을까?
1. PrimaveraApplication
2. SpringApplicationBuilder 에서 SpringApplication 생성 후 run
3. SpringApplication 생성 중 ApplicationListener 등록 (META-INF/spring.factories 파일 참조)
4. ConfigFileApplicationListener 통하여 application.yml 로딩

### Database 어떻게 연결되나?

