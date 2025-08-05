package com.genius.primavera.testingsupport;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

/**
 * 웹 계층과 캐시가 포함된 통합 테스트를 위한 인터페이스
 * 
 * <p>MariaDB + Redis + MockMvc 환경을 제공합니다.</p>
 * 
 * <p>사용법:</p>
 * <pre>
 * {@code
 * @SpringBootTest
 * @ActiveProfiles("test")
 * class YourTestClass implements WebCacheIntegrationTest {
 *     
 *     static {
 *         WebCacheIntegrationTest.startWebCacheContainers();
 *     }
 *     
 *     @Autowired
 *     private MockMvc mockMvc;
 *     
 *     @Autowired
 *     private RedisTemplate<String, String> redisTemplate;
 *     
 *     @Test
 *     void testWebCacheOperation() {
 *         // 웹 요청과 캐시를 함께 사용하는 테스트 코드
 *     }
 * }
 * }
 * </pre>
 */
@AutoConfigureMockMvc
public interface WebCacheIntegrationTest extends MariaDBAndRedisIntegrationTest {

    /**
     * 웹 캐시 테스트에 필요한 모든 컨테이너를 시작합니다.
     */
    static void startWebCacheContainers() {
        MariaDBAndRedisIntegrationTest.mariadbAndRedisStart();
    }
}