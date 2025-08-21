package com.genius.primavera.application.database;

import com.genius.primavera.domain.model.User;
import com.genius.primavera.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.JoinType;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.jpa.QueryHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryOptimizationService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    
    @Transactional(readOnly = true)
    public QueryResult<List<User>> findUsersOptimized() {
        var startTime = Instant.now();
        
        // Using optimized query with fetch joins
        var users = userRepository.findUsersOptimized(true);
        
        var duration = Duration.between(startTime, Instant.now());
        
        return QueryResult.<List<User>>builder()
            .data(users)
            .queryTime(duration.toMillis())
            .resultCount(users.size())
            .optimizationUsed("FETCH JOIN")
            .build();
    }
    
    @Transactional(readOnly = true)
    public QueryResult<List<User>> findUsersWithNPlusOne() {
        var startTime = Instant.now();
        
        // Intentionally bad query for comparison
        var users = userRepository.findAll();
        users.forEach(user -> {
            user.getRoles().size(); // Trigger N+1
            user.getPosts().size(); // Another N+1
        });
        
        var duration = Duration.between(startTime, Instant.now());
        
        return QueryResult.<List<User>>builder()
            .data(users)
            .queryTime(duration.toMillis())
            .resultCount(users.size())
            .optimizationUsed("NONE (N+1 Problem)")
            .build();
    }
    
    @Transactional(readOnly = true)
    public QueryResult<List<User>> findUsersWithEntityGraph() {
        var startTime = Instant.now();
        
        var users = userRepository.findActiveUsersWithGraph();
        
        var duration = Duration.between(startTime, Instant.now());
        
        return QueryResult.<List<User>>builder()
            .data(users)
            .queryTime(duration.toMillis())
            .resultCount(users.size())
            .optimizationUsed("ENTITY GRAPH")
            .build();
    }
    
    @Transactional
    public BatchResult batchInsertUsers(List<UserData> userData) {
        var startTime = Instant.now();
        
        String sql = "INSERT INTO users (email, name, password, active, created_at) VALUES (?, ?, ?, ?, NOW())";
        
        int[] updateCounts = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                UserData data = userData.get(i);
                ps.setString(1, data.getEmail());
                ps.setString(2, data.getName());
                ps.setString(3, data.getPassword());
                ps.setBoolean(4, data.isActive());
            }
            
            @Override
            public int getBatchSize() {
                return userData.size();
            }
        });
        
        var duration = Duration.between(startTime, Instant.now());
        
        return BatchResult.builder()
            .recordsProcessed(updateCounts.length)
            .processingTime(duration.toMillis())
            .batchSize(userData.size())
            .build();
    }
    
    @Transactional(readOnly = true)
    public StreamResult<User> streamLargeDataset() {
        var startTime = Instant.now();
        var processedCount = new int[]{0};
        
        try (Stream<User> stream = userRepository.streamAllUsers()) {
            stream.forEach(user -> {
                // Process each user
                processUser(user);
                processedCount[0]++;
            });
        }
        
        var duration = Duration.between(startTime, Instant.now());
        
        return StreamResult.<User>builder()
            .recordsProcessed(processedCount[0])
            .processingTime(duration.toMillis())
            .streamingUsed(true)
            .build();
    }
    
    @Transactional(readOnly = true)
    public Page<User> searchUsersOptimized(SearchCriteria criteria, Pageable pageable) {
        var cb = entityManager.getCriteriaBuilder();
        var query = cb.createQuery(User.class);
        var root = query.from(User.class);
        
        // Dynamic fetching based on criteria
        if (criteria.isIncludeRoles()) {
            root.fetch("roles", JoinType.LEFT);
        }
        if (criteria.isIncludePosts()) {
            root.fetch("posts", JoinType.LEFT);
        }
        
        var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
        
        if (criteria.getEmail() != null) {
            predicates.add(cb.like(root.get("email"), "%" + criteria.getEmail() + "%"));
        }
        if (criteria.getName() != null) {
            predicates.add(cb.like(root.get("name"), "%" + criteria.getName() + "%"));
        }
        if (criteria.getActive() != null) {
            predicates.add(cb.equal(root.get("active"), criteria.getActive()));
        }
        
        query.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        query.distinct(true);
        
        var typedQuery = entityManager.createQuery(query);
        typedQuery.setHint("hibernate.query.passDistinctThrough", false);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        var results = typedQuery.getResultList();
        
        // Separate count query without fetches
        var countQuery = cb.createQuery(Long.class);
        var countRoot = countQuery.from(User.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        
        var total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new PageImpl<>(results, pageable, total);
    }
    
    @Transactional
    public QueryResult<Integer> updateUsersBatch(List<Long> userIds, boolean active) {
        var startTime = Instant.now();
        
        int updated = userRepository.updateUserStatusBatch(userIds, active);
        
        var duration = Duration.between(startTime, Instant.now());
        
        return QueryResult.<Integer>builder()
            .data(updated)
            .queryTime(duration.toMillis())
            .resultCount(updated)
            .optimizationUsed("BATCH UPDATE")
            .build();
    }
    
    private void processUser(User user) {
        // Simulate processing
        log.trace("Processing user: {}", user.getId());
    }
    
    @Data
    @Builder
    public static class QueryResult<T> {
        private T data;
        private long queryTime;
        private int resultCount;
        private String optimizationUsed;
    }
    
    @Data
    @Builder
    public static class BatchResult {
        private int recordsProcessed;
        private long processingTime;
        private int batchSize;
    }
    
    @Data
    @Builder
    public static class StreamResult<T> {
        private int recordsProcessed;
        private long processingTime;
        private boolean streamingUsed;
    }
    
    @Data
    @Builder
    public static class UserData {
        private String email;
        private String name;
        private String password;
        private boolean active;
    }
    
    @Data
    @Builder
    public static class SearchCriteria {
        private String email;
        private String name;
        private Boolean active;
        private boolean includeRoles;
        private boolean includePosts;
    }
}