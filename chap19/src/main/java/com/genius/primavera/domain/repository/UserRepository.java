package com.genius.primavera.domain.repository;

import com.genius.primavera.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @EntityGraph(value = "User.full", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT u FROM User u WHERE u.active = true")
    List<User> findActiveUsersWithGraph();
    
    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN FETCH u.roles r
        LEFT JOIN FETCH u.posts p
        WHERE u.active = :active
        """)
    List<User> findUsersOptimized(@Param("active") Boolean active);
    
    @Query(value = """
        SELECT u.* FROM users u
        WHERE u.active = true
        ORDER BY u.created_at DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<User> findRecentActiveUsers(@Param("limit") int limit);
    
    @Query("SELECT u FROM User u")
    Stream<User> streamAllUsers();
    
    @Modifying
    @Query("UPDATE User u SET u.active = :active WHERE u.id IN :ids")
    int updateUserStatusBatch(@Param("ids") List<Long> ids, @Param("active") Boolean active);
    
    @Query("""
        SELECT u FROM User u
        LEFT JOIN FETCH u.roles
        WHERE u.email = :email
        """)
    Optional<User> findByEmailWithRoles(@Param("email") String email);
    
    Page<User> findByActiveTrue(Pageable pageable);
}