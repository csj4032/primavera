package com.genius.primavera.test;

import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.testcontainers.containers.MariaDBContainer;

@Slf4j
@Getter
public class TestContainerService {

    private final MariaDBContainer<?> mariaDBContainer;

    // 프로퍼티 기반 생성자
    public TestContainerService(
            @Value("${primavera.testcontainers.database-name:primavera}") String databaseName,
            @Value("${primavera.testcontainers.username:primavera}") String username,
            @Value("${primavera.testcontainers.password:primavera}") String password,
            @Value("${primavera.testcontainers.mariadb-version:mariadb:11.4.7}") String mariadbVersion,
            @Value("${primavera.testcontainers.init-script:sql/schema.sql}") String initScript) {
        
        log.info("Creating TestContainerService with properties - databaseName: {}", databaseName);
        
        this.mariaDBContainer = new MariaDBContainer<>(mariadbVersion)
                .withDatabaseName(databaseName)
                .withUsername(username)
                .withPassword(password)
                .withCommand("--default-authentication-plugin=mysql_native_password");
        
        if (initScript != null && !initScript.isEmpty() && !"none".equals(initScript)) {
            this.mariaDBContainer.withInitScript(initScript);
        }
        
        if (!this.mariaDBContainer.isRunning()) {
            this.mariaDBContainer.start();
        }
    }

    // 기본 생성자 (기존 호환성 유지) - 어노테이션 지원 강화
    public TestContainerService() {
        log.info("Creating TestContainerService with default settings");
        this.mariaDBContainer = createContainerFromAnnotation();
        if (!this.mariaDBContainer.isRunning()) {
            this.mariaDBContainer.start();
        }
    }
    
    private MariaDBContainer<?> createContainerFromAnnotation() {
        // 스택 트레이스에서 어노테이션 찾기 시도
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            try {
                String className = element.getClassName();
                if (className.contains("SpringDataSourceTest") || 
                    (className.contains("Test") && className.contains("dataSource"))) {
                    Class<?> clazz = Class.forName(className);
                    PrimaveraTestContainer annotation = clazz.getAnnotation(PrimaveraTestContainer.class);
                    if (annotation != null) {
                        log.info("어노테이션에서 databaseName 찾음: {}", annotation.databaseName());
                        
                        // 어노테이션 기반으로 컨테이너 생성
                        MariaDBContainer<?> container = new MariaDBContainer<>(annotation.mariadbVersion())
                                .withDatabaseName(annotation.databaseName())
                                .withUsername(annotation.username())
                                .withPassword(annotation.password())
                                .withCommand("--default-authentication-plugin=mysql_native_password");
                        
                        if (annotation.enableInitScript() && !annotation.initScript().isEmpty() && !"none".equals(annotation.initScript())) {
                            container.withInitScript(annotation.initScript());
                        }
                        
                        return container;
                    }
                }
            } catch (Exception e) {
                // 무시하고 계속
            }
        }
        
        // 어노테이션을 찾지 못한 경우 기본 팩토리 사용
        return MariaDBContainerFactory.createFromTestClass();
    }

    public String getJdbcUrl() {
        return mariaDBContainer.getJdbcUrl() + "?allowPublicKeyRetrieval=true&useSSL=false";
    }

    public String getUsername() {
        return mariaDBContainer.getUsername();
    }

    public String getPassword() {
        return mariaDBContainer.getPassword();
    }

    public String getHost() {
        return mariaDBContainer.getHost();
    }

    public Integer getMappedPort() {
        return mariaDBContainer.getMappedPort(3306);
    }

    public boolean isRunning() {
        return mariaDBContainer.isRunning();
    }
}