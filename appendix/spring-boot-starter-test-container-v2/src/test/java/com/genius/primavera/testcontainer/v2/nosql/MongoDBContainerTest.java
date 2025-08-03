package com.genius.primavera.testcontainer.v2.nosql;

import com.genius.primavera.testcontainer.v2.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 케이스: MongoDB 컨테이너 단독 테스트
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableTestContainers(
    containers = {ContainerType.MONGODB},
    lifecycleMode = ContainerLifecycleMode.PER_CLASS
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MongoDB 컨테이너 테스트")
class MongoDBContainerTest extends AutoDynamicPropertySource {

    @Autowired
    private MongoTemplate mongoTemplate;

    // 테스트용 도큐먼트 클래스
    public static class User {
        private String id;
        private String name;
        private String email;
        private Integer age;
        private LocalDateTime createdAt;
        private List<String> tags;

        // 기본 생성자
        public User() {}

        public User(String name, String email, Integer age) {
            this.name = name;
            this.email = email;
            this.age = age;
            this.createdAt = LocalDateTime.now();
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    @Test
    @Order(1)
    @DisplayName("MongoDB 기본 연결 및 도큐먼트 삽입")
    void testMongoDBBasicOperations() {
        // 컬렉션이 비어있는지 확인
        long initialCount = mongoTemplate.count(new Query(), User.class);
        assertEquals(0, initialCount);

        // 도큐먼트 삽입
        User user1 = new User("John Doe", "john@mongodb.com", 30);
        user1.setTags(List.of("developer", "java", "spring"));
        User savedUser = mongoTemplate.save(user1);
        
        assertNotNull(savedUser.getId());
        assertEquals("John Doe", savedUser.getName());
        
        // 개수 확인
        long afterInsertCount = mongoTemplate.count(new Query(), User.class);
        assertEquals(1, afterInsertCount);
        
        log.info("MongoDB basic operations: saved user id={}, count={}", 
                savedUser.getId(), afterInsertCount);
    }

    @Test
    @Order(2)
    @DisplayName("MongoDB 쿼리 작업")
    void testMongoDBQueryOperations() {
        // 추가 데이터 삽입
        User user2 = new User("Jane Smith", "jane@mongodb.com", 25);
        user2.setTags(List.of("designer", "ui", "ux"));
        mongoTemplate.save(user2);
        
        User user3 = new User("Bob Johnson", "bob@mongodb.com", 35);
        user3.setTags(List.of("manager", "scrum", "agile"));
        mongoTemplate.save(user3);
        
        // 이름으로 검색
        Query nameQuery = new Query(Criteria.where("name").is("Jane Smith"));
        User foundUser = mongoTemplate.findOne(nameQuery, User.class);
        assertNotNull(foundUser);
        assertEquals("jane@mongodb.com", foundUser.getEmail());
        
        // 나이 범위로 검색
        Query ageQuery = new Query(Criteria.where("age").gte(30));
        List<User> adultsUsers = mongoTemplate.find(ageQuery, User.class);
        assertEquals(2, adultsUsers.size()); // John(30), Bob(35)
        
        // 태그로 검색
        Query tagQuery = new Query(Criteria.where("tags").in("java"));
        List<User> javaUsers = mongoTemplate.find(tagQuery, User.class);
        assertEquals(1, javaUsers.size());
        assertEquals("John Doe", javaUsers.get(0).getName());
        
        log.info("MongoDB query operations: found by name={}, adults={}, java users={}", 
                foundUser.getName(), adultsUsers.size(), javaUsers.size());
    }

    @Test
    @Order(3)
    @DisplayName("MongoDB 업데이트 작업")
    void testMongoDBUpdateOperations() {
        // 특정 사용자 업데이트
        Query updateQuery = new Query(Criteria.where("name").is("John Doe"));
        Update update = new Update()
                .set("age", 31)
                .addToSet("tags", "mongodb");
        
        var updateResult = mongoTemplate.updateFirst(updateQuery, update, User.class);
        assertEquals(1, updateResult.getModifiedCount());
        
        // 업데이트 결과 확인
        User updatedUser = mongoTemplate.findOne(updateQuery, User.class);
        assertNotNull(updatedUser);
        assertEquals(31, updatedUser.getAge());
        assertTrue(updatedUser.getTags().contains("mongodb"));
        
        // 다중 업데이트
        Query multiUpdateQuery = new Query(Criteria.where("age").gte(30));
        Update multiUpdate = new Update().inc("age", 1);
        var multiUpdateResult = mongoTemplate.updateMulti(multiUpdateQuery, multiUpdate, User.class);
        assertTrue(multiUpdateResult.getModifiedCount() >= 2);
        
        log.info("MongoDB update operations: single update={}, multi update={}", 
                updateResult.getModifiedCount(), multiUpdateResult.getModifiedCount());
    }

    @Test 
    @Order(4)
    @DisplayName("MongoDB 집계 작업")
    void testMongoDBAggregnationOperations() {
        // 평균 나이 계산 (간단한 방식)
        List<User> allUsers = mongoTemplate.findAll(User.class);
        double avgAge = allUsers.stream().mapToInt(User::getAge).average().orElse(0.0);
        assertTrue(avgAge > 0);
        
        // 나이별 그룹핑 (간단한 방식)
        long youngCount = mongoTemplate.count(new Query(Criteria.where("age").lt(30)), User.class);
        long oldCount = mongoTemplate.count(new Query(Criteria.where("age").gte(30)), User.class);
        
        assertEquals(1, youngCount); // Jane(25)
        assertTrue(oldCount >= 2); // John(31+), Bob(35+)
        
        // 태그별 통계 (단순화)
        Query javaQuery = new Query(Criteria.where("tags").in("java"));
        long javaCount = mongoTemplate.count(javaQuery, User.class);
        assertEquals(1, javaCount);
        
        log.info("MongoDB aggregation: avg age={}, young={}, old={}, java users={}", 
                avgAge, youngCount, oldCount, javaCount);
    }

    @Test
    @Order(5)
    @DisplayName("MongoDB 삭제 작업")
    void testMongoDBDeleteOperations() {
        long beforeCount = mongoTemplate.count(new Query(), User.class);
        assertTrue(beforeCount >= 3);
        
        // 조건부 삭제
        Query deleteQuery = new Query(Criteria.where("age").lt(30));
        var deleteResult = mongoTemplate.remove(deleteQuery, User.class);
        assertTrue(deleteResult.getDeletedCount() >= 1);
        
        // 삭제 후 개수 확인
        long afterCount = mongoTemplate.count(new Query(), User.class);
        assertTrue(afterCount < beforeCount);
        
        // 특정 사용자 삭제
        Query specificDeleteQuery = new Query(Criteria.where("name").is("Bob Johnson"));
        var specificDeleteResult = mongoTemplate.remove(specificDeleteQuery, User.class);
        assertEquals(1, specificDeleteResult.getDeletedCount());
        
        long finalCount = mongoTemplate.count(new Query(), User.class);
        
        log.info("MongoDB delete operations: before={}, after first delete={}, final={}", 
                beforeCount, afterCount, finalCount);
    }

    @Test
    @Order(6)
    @DisplayName("MongoDB 성능 테스트")
    void testMongoDBPerformance() {
        // 대량 데이터 삽입
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            User user = new User("User" + i, "user" + i + "@mongodb.com", 20 + (i % 40));
            user.setTags(List.of("tag" + (i % 5), "performance", "test"));
            mongoTemplate.save(user);
        }
        
        long insertTime = System.currentTimeMillis() - startTime;
        
        // 대량 데이터 조회
        startTime = System.currentTimeMillis();
        
        List<User> allUsers = mongoTemplate.findAll(User.class);
        assertTrue(allUsers.size() >= 100);
        
        long readTime = System.currentTimeMillis() - startTime;
        
        // 인덱스가 없는 상황에서의 범위 검색
        startTime = System.currentTimeMillis();
        
        Query rangeQuery = new Query(Criteria.where("age").gte(25).lte(35));
        List<User> rangeUsers = mongoTemplate.find(rangeQuery, User.class);
        assertTrue(rangeUsers.size() > 0);
        
        long queryTime = System.currentTimeMillis() - startTime;
        
        assertTrue(insertTime < 10000, "100 inserts should complete within 10 seconds");
        assertTrue(readTime < 5000, "Reading all documents should complete within 5 seconds");
        assertTrue(queryTime < 5000, "Range query should complete within 5 seconds");
        
        log.info("MongoDB performance: 100 inserts={}ms, read all={}ms, range query={}ms", 
                insertTime, readTime, queryTime);
    }
}