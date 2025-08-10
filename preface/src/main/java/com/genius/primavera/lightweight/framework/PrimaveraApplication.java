package com.genius.primavera.lightweight.framework;

import com.genius.primavera.lightweight.interfaces.PrimaveraApplicationRunner;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Slf4j
public class PrimaveraApplication {
    
    private static PrimaveraApplicationContext applicationContext;
    private static Properties environment;

    public static PrimaveraApplicationContext run(Class<?> primarySource, String... args) {
        printBanner();
        
        long startTime = System.currentTimeMillis();
        log.info(" Primavera translated_text_6 translated_text_2 translated_text_1...");
        
        try {

            loadEnvironment();

            String basePackage = primarySource.getPackage().getName();
            applicationContext = new PrimaveraApplicationContext(basePackage);

            runApplicationRunners();
            
            long endTime = System.currentTimeMillis();
            log.info(" Primavera translated_text_6 translated_text_2 completed! (translated_text_4: {}ms)", endTime - startTime);

            printApplicationInfo();
            
        } catch (Exception e) {
            log.error("translated_text_6 translated_text_2 translated_text_1 error translated_text_2", e);
            throw new RuntimeException("translated_text_6 translated_text_2 failure", e);
        }
        
        return applicationContext;
    }

    private static void printBanner() {
        String banner = """
                
                
                                                                                           
                                   
                             
                             
                             
                                       
                                             
                                                                                           
                        :: Spring Boot Educational Framework ::            v1.0.0     
                                                                                           
                          "translated_text_3 translated_text_2 translated_text_2" - Learning Spring Begins           
                                                                                           
                     DI & IoC • Lifecycle • Configuration • Testing • Architecture    
                                                                                           
                
                
                """;
        System.out.println(banner);
    }

    private static void loadEnvironment() {
        environment = new Properties();
        
        try {

            if (loadYamlConfiguration()) {
                log.info("translated_text_2 translated_text_2 translated_text_2 completed: application.yml (UTF-8)");
            } 

            else if (loadPropertiesConfiguration()) {
                log.info("translated_text_2 translated_text_2 translated_text_2 completed: application.properties (UTF-8)");
            } 

            else {
                log.info("application.yml translated_text_2 application.properties translated_text_3 translated_text_4. translated_text_2 translated_text_2 translated_text_5.");
            }

            environment.putAll(System.getProperties());
            
        } catch (Exception e) {
            log.warn("translated_text_2 translated_text_2 translated_text_2 translated_text_1 error translated_text_2: {}", e.getMessage());
        }
    }

    private static boolean loadYamlConfiguration() {
        try {
            Properties yamlProperties = YamlPropertyLoader.loadYamlAsProperties("application.yml");
            if (!yamlProperties.isEmpty()) {
                environment.putAll(yamlProperties);
                return true;
            }
        } catch (Exception e) {
            log.debug("YAML translated_text_2 translated_text_2 failure: {}", e.getMessage());
        }
        return false;
    }

    private static boolean loadPropertiesConfiguration() {
        try {
            var inputStream = PrimaveraApplication.class.getClassLoader()
                    .getResourceAsStream("application.properties");
            
            if (inputStream != null) {
                try (var reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                    environment.load(reader);
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Properties translated_text_2 translated_text_2 failure: {}", e.getMessage());
        }
        return false;
    }

    private static void runApplicationRunners() {
        if (applicationContext == null) return;
        
        try {
            for (String beanName : applicationContext.getBeanNames()) {
                Object bean = applicationContext.getBean(beanName);
                if (bean instanceof PrimaveraApplicationRunner runner) {
                    log.info("ApplicationRunner execution: {}", beanName);
                    runner.run();
                }
            }
        } catch (Exception e) {
            log.error("ApplicationRunner execution translated_text_1 error translated_text_2", e);
        }
    }

    private static void printApplicationInfo() {
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        log.info("=== Primavera translated_text_6 information ===");
        log.info("translated_text_2 translated_text_2: {}", startTime);
        log.info("Java translated_text_2: {}", System.getProperty("java.version"));
        log.info("translated_text_13 Bean translated_text_1: {}", applicationContext.getBeanNames().size());
        
        if (environment != null) {
            log.info("translated_text_2 translated_text_2 translated_text_1: {}", environment.size());
        }
        
        log.info("================================");
    }

    public static PrimaveraApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static String getProperty(String key) {
        return environment != null ? environment.getProperty(key) : null;
    }

    public static String getProperty(String key, String defaultValue) {
        return environment != null ? environment.getProperty(key, defaultValue) : defaultValue;
    }
}