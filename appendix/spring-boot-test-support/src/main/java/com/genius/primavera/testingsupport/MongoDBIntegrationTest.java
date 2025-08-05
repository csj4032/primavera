package com.genius.primavera.testingsupport;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * MongoDB 통합 테스트를 위한 인터페이스
 * 
 * <p>TestContainers를 사용하여 MongoDB 컨테이너를 자동으로 관리합니다.</p>
 * 
 * <p>사용법:</p>
 * <pre>
 * {@code
 * @SpringBootTest
 * @ActiveProfiles("test")
 * class YourTestClass implements MongoDBIntegrationTest {
 *     
 *     static {
 *         mongodb.start();
 *     }
 *     
 *     @Test
 *     void testMongoDBOperation() {
 *         // MongoDB 테스트 코드
 *     }
 * }
 * }
 * </pre>
 */
@Testcontainers
public interface MongoDBIntegrationTest {

    @Container
    MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0")
            .withReuse(true);

    @DynamicPropertySource
    static void configureMongoDBProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
        registry.add("spring.data.mongodb.database", () -> "test");
    }
}