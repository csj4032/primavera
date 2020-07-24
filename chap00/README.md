## chap00

### @SpringBootConfiguration 

### @EnableAutoConfiguration

### initializers

#### SpringApplicationBuilder 
    1. SpringApplication
    2. run()
        2.1. ConfigurableApplicationContext
        2.2. configureHeadlessProperty()
        2.3. getRunListeners(args)
            2.3.1 SpringFactoriesLoader "META-INF/spring.factories" (파일 위치 확인)
        2.4. ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        2.5. ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
        2.6. configureIgnoreBeanInfo(environment);
        2.7. Banner printedBanner = printBanner(environment);
        2.8. context = createApplicationContext();
        2.9. exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class, new Class[] { ConfigurableApplicationContext.class }, context);
        2.10. prepareContext
                StartupInfoLogger (Starting PrimaveraApplication on Genius-MacBook-Pro.local with PID...)
        2.11. refreshContext
