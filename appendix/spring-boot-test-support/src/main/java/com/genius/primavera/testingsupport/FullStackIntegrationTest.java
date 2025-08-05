package com.genius.primavera.testingsupport;

/**
 * 전체 스택 통합 테스트를 위한 인터페이스
 * 
 * <p>MariaDB, Redis, MongoDB를 모두 포함하는 종합적인 테스트 환경을 제공합니다.</p>
 * 
 * <p>사용법:</p>
 * <pre>
 * {@code
 * @SpringBootTest
 * @ActiveProfiles("test")
 * class YourTestClass implements FullStackIntegrationTest {
 *     
 *     static {
 *         FullStackIntegrationTest.startAllContainers();
 *     }
 *     
 *     @Test
 *     void testFullStackOperation() {
 *         // MariaDB, Redis, MongoDB를 모두 사용하는 테스트 코드
 *     }
 * }
 * }
 * </pre>
 */
public interface FullStackIntegrationTest extends MariaDBIntegrationTest, RedisIntegrationTest, MongoDBIntegrationTest {

    /**
     * 모든 컨테이너를 시작합니다.
     */
    static void startAllContainers() {
        mariadb.start();
        redis.start();
        mongodb.start();
    }
}