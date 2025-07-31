package com.genius.primavera.lightweight.framework;

import com.genius.primavera.lightweight.interfaces.PrimaveraApplicationRunner;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Primavera 경량 프레임워크의 메인 애플리케이션 클래스
 * Spring Boot의 SpringApplication과 유사한 역할을 합니다.
 * 
 * 애플리케이션을 시작하고 라이프사이클을 관리합니다.
 */
@Slf4j
public class PrimaveraApplication {
    
    private static PrimaveraApplicationContext applicationContext;
    private static Properties environment;
    
    /**
     * 애플리케이션을 시작합니다.
     */
    public static PrimaveraApplicationContext run(Class<?> primarySource, String... args) {
        printBanner();
        
        long startTime = System.currentTimeMillis();
        log.info("🌸 Primavera 애플리케이션 시작 중...");
        
        try {
            // 환경 설정 로드
            loadEnvironment();
            
            // ApplicationContext 생성
            String basePackage = primarySource.getPackage().getName();
            applicationContext = new PrimaveraApplicationContext(basePackage);
            
            // ApplicationRunner 실행
            runApplicationRunners();
            
            long endTime = System.currentTimeMillis();
            log.info("🌸 Primavera 애플리케이션 시작 완료! (소요시간: {}ms)", endTime - startTime);
            
            // 애플리케이션 정보 출력
            printApplicationInfo();
            
        } catch (Exception e) {
            log.error("애플리케이션 시작 중 오류 발생", e);
            throw new RuntimeException("애플리케이션 시작 실패", e);
        }
        
        return applicationContext;
    }
    
    /**
     * Primavera 배너를 출력합니다.
     */
    private static void printBanner() {
        String banner = """
                
                ╔═══════════════════════════════════════════════════════════════════════════╗
                ║                                                                           ║
                ║    🌸  ██████╗ ██████╗ ██╗███╗   ███╗ █████╗ ██╗   ██╗███████╗██████╗   ║
                ║       ██╔══██╗██╔══██╗██║████╗ ████║██╔══██╗██║   ██║██╔════╝██╔══██╗  ║
                ║    🌺  ██████╔╝██████╔╝██║██╔████╔██║███████║██║   ██║█████╗  ██████╔╝  ║
                ║       ██╔═══╝ ██╔══██╗██║██║╚██╔╝██║██╔══██║╚██╗ ██╔╝██╔══╝  ██╔══██╗  ║
                ║    🌻  ██║     ██║  ██║██║██║ ╚═╝ ██║██║  ██║ ╚████╔╝ ███████╗██║  ██║  ║
                ║       ╚═╝     ╚═╝  ╚═╝╚═╝╚═╝     ╚═╝╚═╝  ╚═╝  ╚═══╝  ╚══════╝╚═╝  ╚═╝  ║
                ║                                                                           ║
                ║    🌱    :: Spring Boot Educational Framework ::            v1.0.0  🌱   ║
                ║                                                                           ║
                ║         🌿 "배움의 봄이 시작됩니다" - Learning Spring Begins 🌿          ║
                ║                                                                           ║
                ║    🍃 DI & IoC • Lifecycle • Configuration • Testing • Architecture 🍃   ║
                ║                                                                           ║
                ╚═══════════════════════════════════════════════════════════════════════════╝
                
                """;
        System.out.println(banner);
    }
    
    /**
     * 환경 설정을 로드합니다.
     * application.yml 파일을 우선적으로 로드하고, 없으면 application.properties를 시도합니다.
     */
    private static void loadEnvironment() {
        environment = new Properties();
        
        try {
            // 1. application.yml 파일 로드 시도
            if (loadYamlConfiguration()) {
                log.info("환경 설정 로드 완료: application.yml (UTF-8)");
            } 
            // 2. application.properties 파일 로드 시도 (fallback)
            else if (loadPropertiesConfiguration()) {
                log.info("환경 설정 로드 완료: application.properties (UTF-8)");
            } 
            // 3. 설정 파일이 없는 경우
            else {
                log.info("application.yml 또는 application.properties 파일이 없습니다. 기본 설정을 사용합니다.");
            }
            
            // 시스템 프로퍼티 추가
            environment.putAll(System.getProperties());
            
        } catch (Exception e) {
            log.warn("환경 설정 로드 중 오류 발생: {}", e.getMessage());
        }
    }
    
    /**
     * YAML 설정 파일을 로드합니다.
     */
    private static boolean loadYamlConfiguration() {
        try {
            Properties yamlProperties = YamlPropertyLoader.loadYamlAsProperties("application.yml");
            if (!yamlProperties.isEmpty()) {
                environment.putAll(yamlProperties);
                return true;
            }
        } catch (Exception e) {
            log.debug("YAML 파일 로드 실패: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * Properties 설정 파일을 로드합니다.
     */
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
            log.debug("Properties 파일 로드 실패: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * PrimaveraApplicationRunner를 구현한 Bean들을 실행합니다.
     */
    private static void runApplicationRunners() {
        if (applicationContext == null) return;
        
        try {
            for (String beanName : applicationContext.getBeanNames()) {
                Object bean = applicationContext.getBean(beanName);
                if (bean instanceof PrimaveraApplicationRunner runner) {
                    log.info("ApplicationRunner 실행: {}", beanName);
                    runner.run();
                }
            }
        } catch (Exception e) {
            log.error("ApplicationRunner 실행 중 오류 발생", e);
        }
    }
    
    /**
     * 애플리케이션 정보를 출력합니다.
     */
    private static void printApplicationInfo() {
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        log.info("=== Primavera 애플리케이션 정보 ===");
        log.info("시작 시간: {}", startTime);
        log.info("Java 버전: {}", System.getProperty("java.version"));
        log.info("등록된 Bean 수: {}", applicationContext.getBeanNames().size());
        
        if (environment != null) {
            log.info("환경 설정 개수: {}", environment.size());
        }
        
        log.info("================================");
    }
    
    /**
     * 현재 ApplicationContext를 반환합니다.
     */
    public static PrimaveraApplicationContext getApplicationContext() {
        return applicationContext;
    }
    
    /**
     * 환경 변수 값을 조회합니다.
     */
    public static String getProperty(String key) {
        return environment != null ? environment.getProperty(key) : null;
    }
    
    /**
     * 환경 변수 값을 조회합니다. (기본값 포함)
     */
    public static String getProperty(String key, String defaultValue) {
        return environment != null ? environment.getProperty(key, defaultValue) : defaultValue;
    }
}