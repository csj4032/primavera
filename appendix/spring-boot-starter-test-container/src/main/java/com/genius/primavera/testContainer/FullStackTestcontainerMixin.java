package com.genius.primavera.testContainer;

/**
 * 전체 스택 TestContainers Mixin (MariaDB + Redis + Kafka)
 * 
 * 사용법:
 * @SpringBootTest
 * public class MyIntegrationTest implements FullStackTestcontainerMixin {
 *     @Autowired private DataSource dataSource;          // MariaDB
 *     @Autowired private RedisTemplate redisTemplate;    // Redis  
 *     @Autowired private KafkaTemplate kafkaTemplate;    // Kafka
 * }
 * 
 * 개별 컨테이너 정보 접근:
 * - getMariaDBJdbcUrl()
 * - getRedisConnectionString() 
 * - getKafkaBootstrapServers()
 */
public interface FullStackTestcontainerMixin 
    extends MariaDBTestcontainerMixin, RedisTestcontainerMixin, KafkaTestcontainerMixin {
    
    // 모든 컨테이너의 기능을 상속받음
    // 필요시 추가 헬퍼 메서드를 여기에 구현 가능
    
    default void logAllContainerInfo() {
        System.out.println("=== Container Information ===");
        System.out.println("MariaDB: " + getMariaDBJdbcUrl());
        System.out.println("Redis: " + getRedisConnectionString());
        System.out.println("Kafka: " + getKafkaBootstrapServers());
        System.out.println("=============================");
    }
}