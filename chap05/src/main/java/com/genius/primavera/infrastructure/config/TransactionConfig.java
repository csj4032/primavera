package com.genius.primavera.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 트랜잭션 매니저 설정
 * 
 * MyBatis 기반 프로젝트에서 트랜잭션 관리를 위한 설정
 * - DataSourceTransactionManager는 NESTED 트랜잭션을 지원하지만,
 *   테스트 환경에서 호환성 문제로 REQUIRES_NEW 사용 권장
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    /**
     * MyBatis 트랜잭션 매니저 (기본)
     * - NESTED 트랜잭션을 기술적으로 지원하지만 테스트 환경에서는 REQUIRES_NEW 사용
     * - savepoint를 지원하는 데이터베이스(MariaDB/MySQL)에서 동작
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager.setDataSource(dataSource);
        transactionManager.setNestedTransactionAllowed(false); // NESTED 사용 비활성화
        return transactionManager;
    }
}