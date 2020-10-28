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
            2.3.1 SpringFactoriesLoader "META-INF/spring.factories" (파일 위치 확인) 이후 SpringApplicationRunListeners 리스너 등록
                2.3.1.1 getSpringFactoriesInstances
                    2.3.1.1.1 loadFactoryNames (FACTORIES_RESOURCE_LOCATION)
                    2.3.1.1.2 createSpringFactoriesInstances (인스턴스 생성)
                    ("org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer")
                    ("org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener")
                    ("org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer")
                    ("org.springframework.boot.context.ContextIdApplicationContextInitializer" )
                    ("org.springframework.boot.context.config.DelegatingApplicationContextInitializer")
                    ("org.springframework.boot.rsocket.context.RSocketPortInfoApplicationContextInitializer")
                    ("org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer")
        2.4. ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        2.5. ConfigurableEnvironment environment = prepareEnvironment(listeners, applicationArguments);
        2.6. configureIgnoreBeanInfo(environment);
        2.7. Banner printedBanner = printBanner(environment);
        2.8. context = createApplicationContext() DEFAULT_SERVLET_WEB_CONTEXT_CLASS, DEFAULT_REACTIVE_WEB_CONTEXT_CLASS, DEFAULT_CONTEXT_CLASS 
            (ServletWebServerApplicationContext > AnnotationConfigServletWebServerApplicationContext)
            2.8.1 BeanUtils.instantiateClass(contextClass)
        2.9. exceptionReporters = getSpringFactoriesInstances(SpringBootExceptionReporter.class, new Class[] { ConfigurableApplicationContext.class }, context);
        2.10. prepareContext 로그 출력 StartupInfoLogger (Starting PrimaveraApplication on Genius-MacBook-Pro.local with PID...)
            2.10.1 context.setEnvironment(environment)
            2.10.2 postProcessApplicationContext(context)
            2.10.3 applyInitializers(context) : initializers 등록된 initializer initialize 함
            2.10.4 listeners.contextPrepared(context)
        2.11. refreshContext
            2.11.1 refresh()
            2.11.2 this.registerShutdownHook ? context.registerShutdownHook()
        2.12. afterRefresh
        2.13. listeners.started
        2.14. callRunners
        2.15. listeners.running
        2.16. return context

### Reference
1. [ServletWebServerApplicationContext](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/web/servlet/context/ServletWebServerApplicationContext.html)