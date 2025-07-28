package com.genius.primavera.dataSource;

import com.genius.primavera.test.ContainerType;
import com.genius.primavera.test.EnablePrimaveraTestcontainers;
import com.genius.primavera.test.UnifiedTestcontainersMixin;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApplicationContextInitializer 방식을 사용한 간단한 테스트 예시
 * 
 * @EnablePrimaveraTestcontainers 애노테이션만 추가하면
 * ApplicationContextInitializer에서 모든 컨테이너 설정을 자동으로 처리합니다.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers(ContainerType.MARIADB)
@DisplayName("ApplicationContextInitializer 방식 DataSource 테스트")
class SimpleHikariDataSourceTest implements UnifiedTestcontainersMixin {

    @Autowired
    private DataSource dataSource;
    
    @Test
    @DisplayName("MariaDB 컨테이너가 정상적으로 시작되고 DataSource가 정상 작동하는지 확인")
    void testMariaDBConnection() throws Exception {
        // Given: ApplicationContextInitializer에서 시작된 MariaDB 컨테이너
        assertThat(isMariaDBRunning()).isTrue();
        log.info("MariaDB Container URL: {}", getMariaDBJdbcUrl());
        
        // When: DataSource를 통해 데이터베이스 연결
        try (Connection connection = dataSource.getConnection()) {
            
            // Then: 연결이 정상적으로 이루어짐
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(5)).isTrue();
            
            // 간단한 쿼리 실행 테스트
            try (PreparedStatement stmt = connection.prepareStatement("SELECT 1 as test_value")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt("test_value")).isEqualTo(1);
                }
            }
        }
        
        log.info("DataSource connection test passed successfully!");
    }
    
    @Test
    @DisplayName("컨테이너 정보 접근 헬퍼 메서드 테스트")
    void testContainerHelperMethods() {
        // Given: UnifiedTestcontainersMixin의 헬퍼 메서드들
        
        // When & Then: 각 헬퍼 메서드가 정상적으로 작동
        assertThat(getMariaDBHost()).isNotNull();
        assertThat(getMariaDBPort()).isNotNull().isPositive();
        assertThat(getMariaDBJdbcUrl()).isNotNull().contains("jdbc:mysql://");
        
        log.info("MariaDB Host: {}", getMariaDBHost());
        log.info("MariaDB Port: {}", getMariaDBPort());
        log.info("MariaDB JDBC URL: {}", getMariaDBJdbcUrl());
        
        // 모든 컨테이너 정보 출력
        logAllContainerInfo();
    }
}