package com.genius.primavera.testcontainer.v2.multi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 케이스: 다양한 타입의 컨테이너 여러 개 조합 (MariaDB 2개 + Redis 2개 + MongoDB 1개)
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("혼합 다중 컨테이너 - 전체 스택 테스트")
class MixedMultipleContainersTest {

    // 데이터베이스 컨테이너들
    @Container  
    static MariaDBContainer<?> primaryDB = new MariaDBContainer<>("mariadb:11.4.7")
            .withDatabaseName("primary_db")
            .withUsername("primary_user")
            .withPassword("primary_pass")
            .withInitScript("init.sql");

    @Container
    static MariaDBContainer<?> secondaryDB = new MariaDBContainer<>("mariadb:11.4.7")  
            .withDatabaseName("secondary_db")
            .withUsername("secondary_user")
            .withPassword("secondary_pass")
            .withInitScript("init.sql");

    // Redis 컨테이너들
    @Container
    static GenericContainer<?> cacheRedis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "cache_pass");

    @Container
    static GenericContainer<?> sessionRedis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "session_pass");

    // MongoDB 컨테이너
    @Container
    static MongoDBContainer documentDB = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Primary DB
        registry.add("app.primary.datasource.url", primaryDB::getJdbcUrl);
        registry.add("app.primary.datasource.username", primaryDB::getUsername);
        registry.add("app.primary.datasource.password", primaryDB::getPassword);
        
        // Secondary DB
        registry.add("app.secondary.datasource.url", secondaryDB::getJdbcUrl);
        registry.add("app.secondary.datasource.username", secondaryDB::getUsername);
        registry.add("app.secondary.datasource.password", secondaryDB::getPassword);
        
        // Cache Redis
        registry.add("app.cache.redis.host", cacheRedis::getHost);
        registry.add("app.cache.redis.port", () -> cacheRedis.getMappedPort(6379));
        registry.add("app.cache.redis.password", () -> "cache_pass");
        
        // Session Redis
        registry.add("app.session.redis.host", sessionRedis::getHost);
        registry.add("app.session.redis.port", () -> sessionRedis.getMappedPort(6379));
        registry.add("app.session.redis.password", () -> "session_pass");
        
        // Document DB
        registry.add("app.document.mongodb.uri", documentDB::getReplicaSetUrl);
    }

    @TestConfiguration
    static class MixedContainerConfig {
        
        // Primary Database
        @Bean("primaryDataSource")
        public DataSource primaryDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(primaryDB.getJdbcUrl());
            dataSource.setUsername(primaryDB.getUsername());
            dataSource.setPassword(primaryDB.getPassword());
            dataSource.setDriverClassName(primaryDB.getDriverClassName());
            return dataSource;
        }
        
        @Bean("primaryJdbcTemplate") 
        public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
        
        // Secondary Database
        @Bean("secondaryDataSource")
        public DataSource secondaryDataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(secondaryDB.getJdbcUrl());
            dataSource.setUsername(secondaryDB.getUsername());
            dataSource.setPassword(secondaryDB.getPassword());
            dataSource.setDriverClassName(secondaryDB.getDriverClassName());
            return dataSource;
        }
        
        @Bean("secondaryJdbcTemplate")
        public JdbcTemplate secondaryJdbcTemplate(@Qualifier("secondaryDataSource") DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
        
        // Cache Redis
        @Bean("cacheRedisConnectionFactory")
        public RedisConnectionFactory cacheRedisConnectionFactory() {
            LettuceConnectionFactory factory = new LettuceConnectionFactory(
                cacheRedis.getHost(), cacheRedis.getMappedPort(6379));
            factory.setPassword("cache_pass");
            return factory;
        }
        
        @Bean("cacheRedisTemplate")
        public StringRedisTemplate cacheRedisTemplate(@Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }
        
        // Session Redis
        @Bean("sessionRedisConnectionFactory")
        public RedisConnectionFactory sessionRedisConnectionFactory() {
            LettuceConnectionFactory factory = new LettuceConnectionFactory(
                sessionRedis.getHost(), sessionRedis.getMappedPort(6379));
            factory.setPassword("session_pass");
            return factory;
        }
        
        @Bean("sessionRedisTemplate")
        public StringRedisTemplate sessionRedisTemplate(@Qualifier("sessionRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }
        
        // Document Database
        @Bean("mongoTemplate")
        public MongoTemplate mongoTemplate() {
            return new MongoTemplate(new SimpleMongoClientDatabaseFactory(documentDB.getReplicaSetUrl()));
        }
    }

    // 도메인 모델
    public static class UserSession {
        private String id;
        private String userId;
        private LocalDateTime loginTime;
        private String ipAddress;

        public UserSession() {}
        public UserSession(String userId, String ipAddress) {
            this.userId = userId;
            this.ipAddress = ipAddress;
            this.loginTime = LocalDateTime.now();
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public LocalDateTime getLoginTime() { return loginTime; }
        public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    }

    @Autowired @Qualifier("primaryJdbcTemplate") private JdbcTemplate primaryJdbcTemplate;
    @Autowired @Qualifier("secondaryJdbcTemplate") private JdbcTemplate secondaryJdbcTemplate;
    @Autowired @Qualifier("cacheRedisTemplate") private StringRedisTemplate cacheRedisTemplate;
    @Autowired @Qualifier("sessionRedisTemplate") private StringRedisTemplate sessionRedisTemplate;
    @Autowired @Qualifier("mongoTemplate") private MongoTemplate mongoTemplate;

    @Test
    @Order(1)
    @DisplayName("전체 컨테이너 연결 상태 확인")
    void testAllContainerConnections() {
        // Primary DB 연결 확인
        Integer primaryUserCount = primaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, primaryUserCount);
        
        // Secondary DB 연결 확인
        Integer secondaryUserCount = secondaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
        assertEquals(4, secondaryUserCount);
        
        // Cache Redis 연결 확인
        cacheRedisTemplate.opsForValue().set("test:cache", "cache_value");
        String cacheValue = cacheRedisTemplate.opsForValue().get("test:cache");
        assertEquals("cache_value", cacheValue);
        
        // Session Redis 연결 확인
        sessionRedisTemplate.opsForValue().set("test:session", "session_value");
        String sessionValue = sessionRedisTemplate.opsForValue().get("test:session");
        assertEquals("session_value", sessionValue);
        
        // MongoDB 연결 확인
        long documentCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), UserSession.class);
        assertEquals(0, documentCount);
        
        log.info("All connections verified: primaryDB={}, secondaryDB={}, cache={}, session={}, mongo={}",
                primaryUserCount, secondaryUserCount, cacheValue, sessionValue, documentCount);
    }

    @Test
    @Order(2)
    @DisplayName("전체 스택을 활용한 사용자 로그인 시나리오")
    void testFullStackUserLoginScenario() {
        // 1. Primary DB에서 사용자 인증
        String email = "admin@primavera.com";
        String userInfo = primaryJdbcTemplate.queryForObject(
            "SELECT CONCAT(ID, ':', NICKNAME) FROM USERS WHERE EMAIL = ?", 
            String.class, email);
        assertNotNull(userInfo);
        String[] parts = userInfo.split(":");
        String userId = parts[0];
        String nickname = parts[1];
        
        // 2. 세션 생성 (Session Redis)
        String sessionId = "session:" + System.currentTimeMillis();
        Map<String, String> sessionData = new HashMap<>();
        sessionData.put("userId", userId);
        sessionData.put("nickname", nickname);
        sessionData.put("loginTime", LocalDateTime.now().toString());
        
        sessionData.forEach((key, value) -> 
            sessionRedisTemplate.opsForHash().put(sessionId, key, value));
        sessionRedisTemplate.expire(sessionId, java.time.Duration.ofHours(1));
        
        // 3. 사용자 활동 로그 저장 (MongoDB)
        UserSession session = new UserSession(userId, "192.168.1.100");
        mongoTemplate.save(session);
        
        // 4. 캐시에 사용자 정보 저장 (Cache Redis)
        String cacheKey = "user:" + userId;
        cacheRedisTemplate.opsForHash().put(cacheKey, "nickname", nickname);
        cacheRedisTemplate.opsForHash().put(cacheKey, "email", email);
        cacheRedisTemplate.expire(cacheKey, java.time.Duration.ofMinutes(30));
        
        // 5. Secondary DB에 로그인 기록
        secondaryJdbcTemplate.update(
            "UPDATE USERS SET UPDATED_AT = CURRENT_TIMESTAMP WHERE EMAIL = ?", email);
        
        // 검증
        String cachedNickname = (String) cacheRedisTemplate.opsForHash().get(cacheKey, "nickname");
        String sessionNickname = (String) sessionRedisTemplate.opsForHash().get(sessionId, "nickname");
        long mongoSessionCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), UserSession.class);
        
        assertEquals(nickname, cachedNickname);  
        assertEquals(nickname, sessionNickname);
        assertEquals(1, mongoSessionCount);
        
        log.info("Full stack login scenario: user={}, cached={}, session={}, mongo logs={}", 
                nickname, cachedNickname, sessionNickname, mongoSessionCount);
    }

    @Test
    @Order(3)
    @DisplayName("데이터 계층별 특화 작업")
    void testLayerSpecificOperations() {
        // Primary DB: 트랜잭션 기반 비즈니스 로직
        primaryJdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "business@test.com", "{noop}password", "BusinessUser");
        
        // Secondary DB: 분석 및 보고서용 데이터
        secondaryJdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                "analytics@test.com", "{noop}password", "AnalyticsUser");
        
        // Cache Redis: 빈번한 조회 데이터 캐싱
        for (int i = 0; i < 10; i++) {
            cacheRedisTemplate.opsForValue().set("product:" + i, "Product " + i + " Details");
        }
        
        // Session Redis: 사용자 세션 관리
        for (int i = 0; i < 5; i++) {
            String sessionKey = "user_session:" + i;
            sessionRedisTemplate.opsForHash().put(sessionKey, "userId", String.valueOf(i));
            sessionRedisTemplate.opsForHash().put(sessionKey, "status", "active");
        }
        
        // MongoDB: 로그 및 문서 저장
        for (int i = 0; i < 3; i++) {
            UserSession logEntry = new UserSession("user" + i, "192.168.1." + (100 + i));
            mongoTemplate.save(logEntry);
        }
        
        // 검증
        Integer businessUsers = primaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS WHERE EMAIL LIKE '%business%'", Integer.class);
        Integer analyticsUsers = secondaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS WHERE EMAIL LIKE '%analytics%'", Integer.class);
        
        Long cachedProducts = cacheRedisTemplate.keys("product:*").size();
        Long activeSessions = sessionRedisTemplate.keys("user_session:*").size();
        long totalLogs = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), UserSession.class);
        
        assertEquals(1, businessUsers);
        assertEquals(1, analyticsUsers);
        assertEquals(10L, cachedProducts);
        assertEquals(5L, activeSessions);
        assertEquals(4L, totalLogs); // 이전 테스트 1개 + 이번 테스트 3개
        
        log.info("Layer-specific operations: business={}, analytics={}, cached products={}, sessions={}, logs={}", 
                businessUsers, analyticsUsers, cachedProducts, activeSessions, totalLogs);
    }

    @Test
    @Order(4)
    @DisplayName("전체 시스템 성능 테스트")
    void testFullSystemPerformance() {
        long startTime = System.currentTimeMillis();
        
        // 동시에 여러 계층에서 작업 수행
        for (int i = 0; i < 50; i++) {
            // DB 작업
            primaryJdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
            
            // 캐시 작업
            cacheRedisTemplate.opsForValue().set("perf:cache:" + i, "value" + i);
            
            // 세션 작업
            sessionRedisTemplate.opsForValue().set("perf:session:" + i, "session" + i);
            
            // 문서 저장 (매 10번째만)
            if (i % 10 == 0) {
                UserSession perfLog = new UserSession("perf_user_" + i, "192.168.1.200");
                mongoTemplate.save(perfLog);
            }
        }
        
        long endTime = System.currentTimeMillis();
        long totalDuration = endTime - startTime;
        
        // 결과 검증
        Long cacheKeys = cacheRedisTemplate.keys("perf:cache:*").size();
        Long sessionKeys = sessionRedisTemplate.keys("perf:session:*").size();
        long perfLogs = mongoTemplate.count(
            new org.springframework.data.mongodb.core.query.Query(
                org.springframework.data.mongodb.core.query.Criteria.where("userId").regex("perf_user_.*")
            ), UserSession.class);
        
        assertEquals(50L, cacheKeys);
        assertEquals(50L, sessionKeys);
        assertEquals(5L, perfLogs); // 50/10 = 5
        
        assertTrue(totalDuration < 15000, "Full system operations should complete within 15 seconds");
        
        log.info("Full system performance: {}ms for 50 operations across all layers, cache={}, session={}, logs={}", 
                totalDuration, cacheKeys, sessionKeys, perfLogs);
    }

    @Test
    @Order(5)
    @DisplayName("시스템 간 데이터 동기화 및 정합성")
    void testDataConsistencyAcrossSystems() {
        String testUserId = "consistency_test_user";
        String testEmail = "consistency@test.com";
        
        // 1. Primary DB에 사용자 생성
        primaryJdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                testEmail, "{noop}password", testUserId);
        
        Integer userId = primaryJdbcTemplate.queryForObject(
            "SELECT ID FROM USERS WHERE EMAIL = ?", Integer.class, testEmail);
        
        // 2. Secondary DB에 동일한 사용자 정보 복제
        secondaryJdbcTemplate.update("INSERT INTO USERS (EMAIL, PASSWORD, NICKNAME) VALUES (?, ?, ?)",
                testEmail, "{noop}password", testUserId);
        
        // 3. 캐시에 사용자 정보 저장
        String cacheKey = "user:" + userId;
        cacheRedisTemplate.opsForHash().put(cacheKey, "email", testEmail);
        cacheRedisTemplate.opsForHash().put(cacheKey, "nickname", testUserId);
        
        // 4. 세션 생성
        String sessionKey = "session:" + userId;
        sessionRedisTemplate.opsForHash().put(sessionKey, "userId", userId.toString());
        sessionRedisTemplate.opsForHash().put(sessionKey, "email", testEmail);
        
        // 5. MongoDB에 활동 로그
        UserSession activityLog = new UserSession(userId.toString(), "192.168.1.300");
        mongoTemplate.save(activityLog);
        
        // 정합성 검증
        String primaryEmail = primaryJdbcTemplate.queryForObject(
            "SELECT EMAIL FROM USERS WHERE ID = ?", String.class, userId);
        String secondaryEmail = secondaryJdbcTemplate.queryForObject(
            "SELECT EMAIL FROM USERS WHERE ID = ?", String.class, userId);
        String cachedEmail = (String) cacheRedisTemplate.opsForHash().get(cacheKey, "email");
        String sessionEmail = (String) sessionRedisTemplate.opsForHash().get(sessionKey, "email");
        
        UserSession savedLog = mongoTemplate.findOne(
            new org.springframework.data.mongodb.core.query.Query(
                org.springframework.data.mongodb.core.query.Criteria.where("userId").is(userId.toString())
            ), UserSession.class);
        
        // 모든 시스템에서 동일한 이메일이 조회되어야 함
        assertEquals(testEmail, primaryEmail);
        assertEquals(testEmail, secondaryEmail);
        assertEquals(testEmail, cachedEmail);
        assertEquals(testEmail, sessionEmail);
        assertNotNull(savedLog);
        assertEquals(userId.toString(), savedLog.getUserId());
        
        log.info("Data consistency verified: primary={}, secondary={}, cache={}, session={}, mongo user={}", 
                primaryEmail, secondaryEmail, cachedEmail, sessionEmail, savedLog.getUserId());
    }
}