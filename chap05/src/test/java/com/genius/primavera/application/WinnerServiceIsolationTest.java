package com.genius.primavera.application;

import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.Winner;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.*;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers
@DisplayName(value = "트랜잭션 Isolation 테스트")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WinnerServiceIsolationTest {

    private TransactionStatus status;
    private static SqlSession sqlSession;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeAll
    public void before() {
        sqlSession = sqlSessionFactory.openSession();
        WinnerMapper winnerMapper = sqlSession.getMapper(WinnerMapper.class);
        winnerMapper.truncate();
    }

    @Test
    @Order(1)
    @DisplayName("READ_UNCOMMITTED_INSERT")
    public void read_uncommitted_insert() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("READ_UNCOMMITTED_INSERT");
        definition.setPropagationBehavior(0);
        status = transactionManager.getTransaction(definition);
        sqlSession = sqlSessionFactory.openSession();
        WinnerMapper winnerMapper = sqlSession.getMapper(WinnerMapper.class);
        winnerMapper.save(Winner.builder()
                .name("John Doe")
                .year(2023)
                .sport("Test Sport 1")
                .prize("Test Prize 1")
                .amount(new BigDecimal("100000.00"))
                .build());
        winnerMapper.save(Winner.builder()
                .name("Jane Smith")
                .year(2023)
                .sport("Test Sport 2")
                .prize("Test Prize 2")
                .amount(new BigDecimal("200000.00"))
                .build());
    }

    @Test
    @Order(2)
    @DisplayName("READ_COMMITTED_SELECT")
    public void read_committed() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("READ_COMMITTED_SELECT");
        definition.setPropagationBehavior(3);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        TransactionStatus status = transactionManager.getTransaction(definition);
        SqlSession localSqlSession = sqlSessionFactory.openSession();
        WinnerMapper winnerMapper = localSqlSession.getMapper(WinnerMapper.class);
        List<Winner> winnerList = winnerMapper.findAll();
        transactionManager.commit(status);
        Assertions.assertTrue(winnerList.isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("READ_UNCOMMITTED_SELECT")
    public void read_uncommitted() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("READ_COMMITTED_SELECT");
        definition.setPropagationBehavior(0);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = transactionManager.getTransaction(definition);
        SqlSession localSqlSession = sqlSessionFactory.openSession();
        WinnerMapper winnerMapper = localSqlSession.getMapper(WinnerMapper.class);
        List<Winner> winnerList = winnerMapper.findAll();
        transactionManager.commit(status);
        Assertions.assertFalse(winnerList.isEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("READ_UNCOMMITTED_COMMIT")
    public void read_uncommitted_commit() {
        transactionManager.commit(status);
    }

    @Test
    @Order(5)
    @DisplayName("REPEATABLE_READ_READ_COMMITTED")
    public void read_committed_repeatable() throws SQLException {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("REPEATABLE_READ_READ_COMMITTED");
        definition.setPropagationBehavior(0);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        TransactionStatus status = transactionManager.getTransaction(definition);
        SqlSession localSqlSession = sqlSessionFactory.openSession();
        Winner before = localSqlSession.getMapper(WinnerMapper.class).findById(1L);

        DefaultTransactionDefinition definition2 = new DefaultTransactionDefinition();
        definition2.setName("REPEATABLE_READ_READ_COMMITTED_UPDATE");
        definition2.setPropagationBehavior(3);
        TransactionStatus status2 = transactionManager.getTransaction(definition2);
        SqlSession localSqlSession2 = sqlSessionFactory.openSession();
        WinnerMapper localWinnerMapper = localSqlSession2.getMapper(WinnerMapper.class);
        localWinnerMapper.update(Winner.builder()
                .id(1L)
                .name("John Doe Updated")
                .year(2023)
                .sport("Test Sport 1")
                .prize("Updated Prize")
                .amount(new BigDecimal("150000.00"))
                .build());
        transactionManager.commit(status2);

        localSqlSession.clearCache();
        Winner after = localSqlSession.getMapper(WinnerMapper.class).findById(1L);
        transactionManager.commit(status);
        Assertions.assertNotEquals(before.getPrize(), after.getPrize());
    }

    @Test
    @Order(6)
    @DisplayName("REPEATABLE_READ_REPEATABLE_READ")
    public void repeatable_read_select() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("REPEATABLE_READ_REPEATABLE_READ");
        definition.setPropagationBehavior(0);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        TransactionStatus status = transactionManager.getTransaction(definition);
        SqlSession localSqlSession = sqlSessionFactory.openSession();
        Winner before = localSqlSession.getMapper(WinnerMapper.class).findById(1L);

        DefaultTransactionDefinition definition2 = new DefaultTransactionDefinition();
        definition2.setName("REPEATABLE_READ_REPEATABLE_READ_UPDATE");
        definition2.setPropagationBehavior(3);
        TransactionStatus status2 = transactionManager.getTransaction(definition2);
        SqlSession localSqlSession2 = sqlSessionFactory.openSession();
        WinnerMapper localWinnerMapper = localSqlSession2.getMapper(WinnerMapper.class);
        localWinnerMapper.update(Winner.builder()
                .id(1L)
                .name("John Doe")
                .year(2023)
                .sport("Test Sport 1")
                .prize("Original Prize")
                .amount(new BigDecimal("100000.00"))
                .build());
        transactionManager.commit(status2);

        localSqlSession.clearCache();
        Winner after = localSqlSession.getMapper(WinnerMapper.class).findById(1L);
        transactionManager.commit(status);
        Assertions.assertEquals(before.getPrize(), after.getPrize());
    }

    @Test
    @Order(7)
    @DisplayName("REPEATABLE_READ_REPEATABLE_READ_INSERT")
    public void repeatable_read_insert() {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("REPEATABLE_READ_REPEATABLE_READ_INSERT");
        definition.setPropagationBehavior(0);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        definition.setReadOnly(true);
        TransactionStatus status = transactionManager.getTransaction(definition);
        SqlSession localSqlSession = sqlSessionFactory.openSession();
        int beforeWinnerList = localSqlSession.getMapper(WinnerMapper.class).findByIdGtCount(0L);

        DefaultTransactionDefinition definition2 = new DefaultTransactionDefinition();
        definition2.setName("REPEATABLE_READ_REPEATABLE_READ_INSERT_INSERT");
        definition2.setPropagationBehavior(3);
        definition2.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        TransactionStatus status2 = transactionManager.getTransaction(definition2);
        SqlSession localSqlSession2 = sqlSessionFactory.openSession();
        WinnerMapper localWinnerMapper = localSqlSession2.getMapper(WinnerMapper.class);
        localWinnerMapper.save(Winner.builder()
                .name("Michael Johnson")
                .year(2023)
                .sport("Athletics")
                .prize("Olympic Gold")
                .amount(new BigDecimal("300000.00"))
                .build());
        localWinnerMapper.save(Winner.builder()
                .name("Sarah Williams")
                .year(2023)
                .sport("Swimming")
                .prize("World Championship")
                .amount(new BigDecimal("250000.00"))
                .build());
        transactionManager.commit(status2);

        localSqlSession.clearCache();
        int afterWinnerList = localSqlSession.getMapper(WinnerMapper.class).findByIdGtCount(0L);
        transactionManager.commit(status);
        // In MariaDB, REPEATABLE_READ with read-only transaction should not see phantom reads
        // but practical behavior may vary, so we check that the count doesn't change
        Assertions.assertEquals(beforeWinnerList, afterWinnerList);
    }

    @Test
    @Order(8)
    @DisplayName("SERIALIZABLE_INSERT")
    public void serializable_read_insert() {
        // SERIALIZABLE 격리 수준에서는 최고 수준의 격리를 제공
        // 간단한 시나리오로 테스트: 트랜잭션 내에서 데이터 조회 후 동일한 범위 재조회
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setName("SERIALIZABLE");
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        definition.setTimeout(10); // 10초 타임아웃 설정
        
        TransactionStatus status = transactionManager.getTransaction(definition);
        SqlSession localSqlSession = sqlSessionFactory.openSession();
        WinnerMapper winnerMapper = localSqlSession.getMapper(WinnerMapper.class);
        
        // 현재 데이터 조회
        List<Winner> beforeList = winnerMapper.findByIdGt(0L);
        log.info("SERIALIZABLE 트랜잭션 시작 - 현재 데이터 수: {}", beforeList.size());
        
        // 같은 트랜잭션 내에서 데이터 추가
        winnerMapper.save(Winner.builder()
                .name("Serializable Test Winner")
                .year(2023)
                .sport("Test Sport")
                .prize("Test Prize")
                .amount(new BigDecimal("100000.00"))
                .build());
        
        // 다시 조회 - 같은 트랜잭션 내에서는 추가된 데이터가 보여야 함
        List<Winner> afterList = winnerMapper.findByIdGt(0L);
        log.info("SERIALIZABLE 트랜잭션 - 추가 후 데이터 수: {}", afterList.size());
        
        transactionManager.commit(status);
        
        // 트랜잭션 내에서 추가한 데이터가 반영되어야 함
        Assertions.assertEquals(beforeList.size() + 1, afterList.size());
        log.info("SERIALIZABLE 테스트 통과 - 트랜잭션 내 일관성 유지");
    }
}