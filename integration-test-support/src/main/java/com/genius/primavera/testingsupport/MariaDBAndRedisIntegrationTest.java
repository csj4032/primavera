package com.genius.primavera.testingsupport;

public interface MariaDBAndRedisIntegrationTest extends MariaDBIntegrationTest, RedisIntegrationTest {

    static void mariadbAndRedisStart() {
        mariadb.start();
        redis.start();
    }
}
