## chap00

### @SpringBootConfiguration 

### @EnableAutoConfiguration

### initializers

#### SpringApplicationBuilder 
    1. SpringApplication
    1. run()
        1.1. ConfigurableApplicationContext
        1.2. configureHeadlessProperty()
        1.3. getRunListeners(args)
            1.3.1 SpringFactoriesLoader "META-INF/spring.factories" (파일 위치 확인)
        ConfigFileApplicationListener
        getSpringFactoriesInstances
