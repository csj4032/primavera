package com.genius.primavera.batch.repository;

import com.genius.primavera.common.domain.Product;
import com.genius.primavera.common.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.seller s " +
           "JOIN FETCH p.category c " +
           "WHERE p.id = :id")
    Optional<Product> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.seller " +
           "JOIN FETCH p.category " +
           "WHERE p.status = :status")
    List<Product> findAllByStatusWithDetails(@Param("status") ProductStatus status);

    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.seller " +
           "JOIN FETCH p.category " +
           "WHERE p.updatedAt > :lastModified")
    List<Product> findModifiedProductsSince(@Param("lastModified") LocalDateTime lastModified);

    @Query("SELECT p FROM Product p " +
           "WHERE p.seller.id = :sellerId")
    Page<Product> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    @Query("SELECT p FROM Product p " +
           "WHERE p.category.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = :status")
    long countByStatus(@Param("status") ProductStatus status);

    @Query("SELECT p FROM Product p " +
           "JOIN FETCH p.seller " +
           "JOIN FETCH p.category " +
           "WHERE p.price BETWEEN :minPrice AND :maxPrice " +
           "AND p.status = :status")
    List<Product> findByPriceRangeAndStatus(
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("status") ProductStatus status);
}