package com.genius.primavera.testContainer;

/**
 * 데이터베이스 전용 TestContainers Mixin (MariaDB + Redis)
 * 
 * 사용법:
 * @SpringBootTest
 * public class MyDatabaseTest implements DatabaseTestcontainerMixin {
 *     @Autowired private DataSource dataSource;       // MariaDB
 *     @Autowired private RedisTemplate redisTemplate; // Redis
 * }
 */
public interface DatabaseTestcontainerMixin 
    extends MariaDBTestcontainerMixin, RedisTestcontainerMixin {
    
    // MariaDB + Redis 기능 조합
}