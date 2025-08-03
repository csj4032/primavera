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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 케이스: 2개의 MongoDB 컨테이너와 각각 대응하는 MongoTemplate 설정
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("이중 MongoDB 컨테이너 - 다중 MongoTemplate")
class DualMongoDBContainersTest {

    @Container
    static MongoDBContainer mongo1 = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    @Container
    static MongoDBContainer mongo2 = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MongoDB 1 설정
        registry.add("app.mongo1.uri", mongo1::getReplicaSetUrl);
        
        // MongoDB 2 설정  
        registry.add("app.mongo2.uri", mongo2::getReplicaSetUrl);
    }

    @TestConfiguration
    static class DualMongoConfig {
        
        @Bean("mongo1Template")
        public MongoTemplate mongo1Template() {
            return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongo1.getReplicaSetUrl()));
        }
        
        @Bean("mongo2Template")
        public MongoTemplate mongo2Template() {
            return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongo2.getReplicaSetUrl()));
        }
    }

    // 테스트용 도큐먼트 클래스들
    public static class User {
        private String id;
        private String name;
        private String email;
        private Integer age;
        private LocalDateTime createdAt;

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
    }

    public static class Product {
        private String id;
        private String name;
        private Double price;
        private Integer stock;
        private String category;

        public Product() {}

        public Product(String name, Double price, Integer stock, String category) {
            this.name = name;
            this.price = price;
            this.stock = stock;
            this.category = category;
        }

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    @Autowired
    @Qualifier("mongo1Template")
    private MongoTemplate mongo1Template;

    @Autowired
    @Qualifier("mongo2Template")
    private MongoTemplate mongo2Template;

    @Test
    @Order(1)
    @DisplayName("두 MongoDB 컨테이너 연결 확인")
    void testDualMongoConnections() {
        // Mongo 1 연결 테스트
        long mongo1Count = mongo1Template.count(new Query(), User.class);
        assertEquals(0, mongo1Count);
        
        // Mongo 2 연결 테스트
        long mongo2Count = mongo2Template.count(new Query(), Product.class);
        assertEquals(0, mongo2Count);
        
        log.info("Dual MongoDB connections: mongo1 users={}, mongo2 products={}", mongo1Count, mongo2Count);
    }

    @Test
    @Order(2)
    @DisplayName("각 MongoDB에 서로 다른 도메인 데이터 저장")
    void testDifferentDomainData() {
        // Mongo 1: 사용자 데이터
        User user1 = new User("Alice", "alice@mongo1.com", 25);
        User user2 = new User("Bob", "bob@mongo1.com", 30);
        
        mongo1Template.save(user1);
        mongo1Template.save(user2);
        
        // Mongo 2: 상품 데이터
        Product product1 = new Product("Laptop", 1500.0, 10, "Electronics");
        Product product2 = new Product("Book", 25.0, 100, "Education");
        
        mongo2Template.save(product1);
        mongo2Template.save(product2);
        
        // 독립성 확인
        long mongo1UserCount = mongo1Template.count(new Query(), User.class);
        long mongo2ProductCount = mongo2Template.count(new Query(), Product.class);
        
        assertEquals(2, mongo1UserCount);
        assertEquals(2, mongo2ProductCount);
        
        // 다른 컬렉션에서는 데이터가 보이지 않아야 함
        long mongo1ProductCount = mongo1Template.count(new Query(), Product.class);
        long mongo2UserCount = mongo2Template.count(new Query(), User.class);
        
        assertEquals(0, mongo1ProductCount);
        assertEquals(0, mongo2UserCount);
        
        log.info("Different domain data: mongo1 users={}, mongo2 products={}, cross-check passed", 
                mongo1UserCount, mongo2ProductCount);
    }

    @Test
    @Order(3)
    @DisplayName("각 MongoDB에서 서로 다른 쿼리 패턴 사용")
    void testDifferentQueryPatterns() {
        // Mongo 1: 사용자 검색 (나이 기반)
        Query ageQuery = new Query(Criteria.where("age").gte(25));
        List<User> adultUsers = mongo1Template.find(ageQuery, User.class);
        assertEquals(2, adultUsers.size());
        
        Query youngQuery = new Query(Criteria.where("age").lt(30));
        List<User> youngUsers = mongo1Template.find(youngQuery, User.class);
        assertEquals(1, youngUsers.size());
        assertEquals("Alice", youngUsers.get(0).getName());
        
        // Mongo 2: 상품 검색 (가격 범위 및 카테고리)
        Query priceQuery = new Query(Criteria.where("price").gte(100.0));
        List<Product> expensiveProducts = mongo2Template.find(priceQuery, Product.class);
        assertEquals(1, expensiveProducts.size());
        assertEquals("Laptop", expensiveProducts.get(0).getName());
        
        Query categoryQuery = new Query(Criteria.where("category").is("Education"));
        List<Product> educationProducts = mongo2Template.find(categoryQuery, Product.class);
        assertEquals(1, educationProducts.size());
        assertEquals("Book", educationProducts.get(0).getName());
        
        log.info("Different query patterns: mongo1 adults={}, young={}, mongo2 expensive={}, education={}", 
                adultUsers.size(), youngUsers.size(), expensiveProducts.size(), educationProducts.size());
    }

    @Test
    @Order(4)
    @DisplayName("각 MongoDB에서 집계(Aggregation) 작업")
    void testAggregationOperations() {
        // 추가 데이터 삽입
        mongo1Template.save(new User("Charlie", "charlie@mongo1.com", 35));
        mongo1Template.save(new User("Diana", "diana@mongo1.com", 28));
        
        mongo2Template.save(new Product("Phone", 800.0, 50, "Electronics"));
        mongo2Template.save(new Product("Pen", 5.0, 200, "Office"));
        
        // Mongo 1: 사용자 통계
        List<User> allUsers = mongo1Template.findAll(User.class);
        double avgAge = allUsers.stream().mapToInt(User::getAge).average().orElse(0.0);
        long youngCount = mongo1Template.count(new Query(Criteria.where("age").lt(30)), User.class);
        
        assertTrue(avgAge > 25);
        assertEquals(2, youngCount); // Alice(25), Diana(28)
        
        // Mongo 2: 상품 통계
        List<Product> allProducts = mongo2Template.findAll(Product.class);
        double avgPrice = allProducts.stream().mapToDouble(Product::getPrice).average().orElse(0.0);
        long electronicsCount = mongo2Template.count(new Query(Criteria.where("category").is("Electronics")), Product.class);
        
        assertTrue(avgPrice > 300);
        assertEquals(2, electronicsCount); // Laptop, Phone
        
        log.info("Aggregation operations: mongo1 avg age={}, young users={}, mongo2 avg price={}, electronics={}", 
                avgAge, youngCount, avgPrice, electronicsCount);
    }

    @Test
    @Order(5)
    @DisplayName("두 MongoDB 간 데이터 마이그레이션 시뮬레이션")
    void testDataMigration() {
        // Mongo 1에서 특정 사용자 조회
        Query userQuery = new Query(Criteria.where("name").is("Alice"));
        User alice = mongo1Template.findOne(userQuery, User.class);
        assertNotNull(alice);
        
        // Alice 정보를 기반으로 Mongo 2에 관련 상품 생성 (사용자 맞춤 상품)
        Product customProduct = new Product(
            "Custom Product for " + alice.getName(),
            100.0 + alice.getAge() * 10, // 나이 기반 가격
            alice.getAge(),              // 나이 기반 재고
            "Custom"
        );
        
        mongo2Template.save(customProduct);
        
        // 마이그레이션 결과 확인
        Query customQuery = new Query(Criteria.where("category").is("Custom"));
        List<Product> customProducts = mongo2Template.find(customQuery, Product.class);
        
        assertEquals(1, customProducts.size());
        Product savedCustom = customProducts.get(0);
        assertTrue(savedCustom.getName().contains("Alice"));
        assertEquals(350.0, savedCustom.getPrice()); // 100 + 25*10
        
        log.info("Data migration: user {} -> custom product {}, price={}", 
                alice.getName(), savedCustom.getName(), savedCustom.getPrice());
    }

    @Test
    @Order(6)
    @DisplayName("두 MongoDB 성능 비교")
    void testPerformanceComparison() {
        // Mongo 1 성능 테스트 (사용자 데이터)
        long mongo1Start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            User user = new User("User" + i, "user" + i + "@mongo1.com", 20 + (i % 50));
            mongo1Template.save(user);
        }
        long mongo1InsertTime = System.currentTimeMillis() - mongo1Start;
        
        // Mongo 2 성능 테스트 (상품 데이터)
        long mongo2Start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            Product product = new Product("Product" + i, 10.0 + i, 10 + i, "Category" + (i % 5));
            mongo2Template.save(product);
        }
        long mongo2InsertTime = System.currentTimeMillis() - mongo2Start;
        
        // 조회 성능 테스트
        mongo1Start = System.currentTimeMillis();
        List<User> allUsers = mongo1Template.findAll(User.class);
        long mongo1ReadTime = System.currentTimeMillis() - mongo1Start;
        
        mongo2Start = System.currentTimeMillis();
        List<Product> allProducts = mongo2Template.findAll(Product.class);
        long mongo2ReadTime = System.currentTimeMillis() - mongo2Start;
        
        assertTrue(allUsers.size() >= 100);
        assertTrue(allProducts.size() >= 100);
        
        assertTrue(mongo1InsertTime < 10000, "Mongo1 100 inserts should complete within 10 seconds");
        assertTrue(mongo2InsertTime < 10000, "Mongo2 100 inserts should complete within 10 seconds");
        assertTrue(mongo1ReadTime < 5000, "Mongo1 read all should complete within 5 seconds");
        assertTrue(mongo2ReadTime < 5000, "Mongo2 read all should complete within 5 seconds");
        
        log.info("Performance comparison: mongo1 insert={}ms, read={}ms, mongo2 insert={}ms, read={}ms", 
                mongo1InsertTime, mongo1ReadTime, mongo2InsertTime, mongo2ReadTime);
    }
}