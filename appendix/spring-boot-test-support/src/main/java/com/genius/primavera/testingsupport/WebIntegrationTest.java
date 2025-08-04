package com.genius.primavera.testingsupport;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 웹 컨트롤러 통합 테스트를 위한 인터페이스
 * 
 * <p>MariaDB TestContainer와 MockMvc를 함께 제공합니다.</p>
 * 
 * <h3>사용 예시:</h3>
 * <pre>
 * &#64;SpringBootTest
 * &#64;ActiveProfiles("test")
 * &#64;TestInstance(TestInstance.Lifecycle.PER_CLASS)
 * public class UserControllerTest implements WebIntegrationTest {
 *     static {
 *         mariadb.start();
 *     }
 *     
 *     &#64;Autowired
 *     private MockMvc mockMvc;
 * }
 * </pre>
 */
@Testcontainers
@AutoConfigureMockMvc
public interface WebIntegrationTest {

    @Container
    MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4.7")
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariadb::getJdbcUrl);
        registry.add("spring.datasource.username", mariadb::getUsername);
        registry.add("spring.datasource.password", mariadb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariadb::getDriverClassName);
    }
}