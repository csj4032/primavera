package com.genius.primavera.test;

import com.genius.primavera.test.condition.PrimaveraTestContainerCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import com.genius.primavera.test.annotation.PrimaveraTestContainer;
import org.testcontainers.containers.MariaDBContainer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(MariaDBContainer.class)
@ConditionalOnProperty(name = "primavera.testcontainers.enabled", havingValue = "true", matchIfMissing = false)
@Import({TestContainerAutoConfiguration.DataSourceConfiguration.class})
public class TestContainerAutoConfiguration {

    // @PrimaveraTestContainer 어노테이션이 있는지 확인하는 메서드
    private boolean hasPrimaveraTestContainerAnnotation() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                if (clazz.isAnnotationPresent(PrimaveraTestContainer.class)) {
                    return true;
                }
            } catch (ClassNotFoundException e) {
                // 무시
            }
        }
        return false;
    }

    @Bean
    @Conditional(PrimaveraTestContainerCondition.class)
    @ConditionalOnProperty(name = "primavera.testcontainers.service.enabled", havingValue = "true", matchIfMissing = true)
    public TestContainerService testContainerService(ConfigurableEnvironment environment) {
        // 스택 트레이스에서 테스트 클래스 찾아서 어노테이션 읽기
        String databaseName = "primavera";
        String username = "primavera";
        String password = "primavera";
        String mariadbVersion = "mariadb:11.4.7";
        String initScript = "sql/schema.sql";
        
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                PrimaveraTestContainer annotation = AnnotationUtils.findAnnotation(clazz, PrimaveraTestContainer.class);
                if (annotation != null) {
                    databaseName = annotation.databaseName();
                    username = annotation.username();
                    password = annotation.password();
                    mariadbVersion = annotation.mariadbVersion();
                    initScript = annotation.enableInitScript() ? annotation.initScript() : "none";
                    log.info("어노테이션에서 찾은 설정값 - databaseName: {}, username: {}, password: {}", databaseName, username, password);
                    break;
                }
            } catch (ClassNotFoundException e) {
                // 무시
            }
        }
        
        // 환경 변수에서도 확인 (우선순위: 환경변수 > 어노테이션)
        databaseName = environment.getProperty("primavera.testcontainers.database-name", databaseName);
        username = environment.getProperty("primavera.testcontainers.username", username);  
        password = environment.getProperty("primavera.testcontainers.password", password);
        mariadbVersion = environment.getProperty("primavera.testcontainers.mariadb-version", mariadbVersion);
        initScript = environment.getProperty("primavera.testcontainers.init-script", initScript);
        
        log.info("최종 TestContainerService 생성 - databaseName: {}, username: {}, password: {}", databaseName, username, password);
        return new TestContainerService(databaseName, username, password, mariadbVersion, initScript);
    }

    @Bean
    @ConditionalOnBean(TestContainerService.class)
    public MariaDBContainer<?> mariaDBContainer(TestContainerService testContainerService) {
        return testContainerService.getMariaDBContainer();
    }

    @Configuration
    @ConditionalOnBean(TestContainerService.class)
    public static class DataSourceConfiguration {

        @Bean
        @ConditionalOnBean(TestContainerService.class)
        public MapPropertySource testContainerDataSourceProperties(TestContainerService testContainerService, ConfigurableEnvironment environment) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("spring.datasource.url", testContainerService.getJdbcUrl());
            properties.put("spring.datasource.username", testContainerService.getUsername());
            properties.put("spring.datasource.password", testContainerService.getPassword());
            properties.put("spring.datasource.driver-class-name", "org.mariadb.jdbc.Driver");
            
            // 기존 프로퍼티 소스를 제거하고 새로 추가하여 우선순위 보장
            String propertySourceName = "testcontainer-datasource";
            if (environment.getPropertySources().contains(propertySourceName)) {
                environment.getPropertySources().remove(propertySourceName);
            }
            
            MapPropertySource propertySource = new MapPropertySource(propertySourceName, properties);
            environment.getPropertySources().addFirst(propertySource);
            
            log.info("TestContainers DataSource 설정 완료 - URL: {}, databaseName from URL: {}", 
                testContainerService.getJdbcUrl(),
                testContainerService.getJdbcUrl().substring(testContainerService.getJdbcUrl().lastIndexOf("/") + 1, 
                    testContainerService.getJdbcUrl().indexOf("?") > 0 ? testContainerService.getJdbcUrl().indexOf("?") : testContainerService.getJdbcUrl().length()));
            
            // 프로퍼티 소스 우선순위 확인 로그
            log.info("현재 프로퍼티 소스 우선순위:");
            environment.getPropertySources().forEach(ps -> 
                log.info("  - {}: {}", ps.getName(), ps.getClass().getSimpleName()));
                
            return propertySource;
        }

        @Bean
        @ConditionalOnBean(TestContainerService.class)
        public javax.sql.DataSource dataSource(TestContainerService testContainerService) {
            com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();
            config.setJdbcUrl(testContainerService.getJdbcUrl());
            config.setUsername(testContainerService.getUsername());
            config.setPassword(testContainerService.getPassword());
            config.setDriverClassName("org.mariadb.jdbc.Driver");
            log.info("TestContainers DataSource Bean 생성 완료: {}", testContainerService.getJdbcUrl());
            return new com.zaxxer.hikari.HikariDataSource(config);
        }
    }

}