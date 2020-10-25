package com.genius.primavera.application;

import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.Winner;
import com.genius.primavera.domain.model.WinnerType;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;
import static java.sql.Connection.TRANSACTION_REPEATABLE_READ;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName(value = "트랜잭션 Isolation 테스트")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
		winnerMapper.insertWinner(Winner.builder().id(1).userId(1).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build());
		winnerMapper.insertWinner(Winner.builder().id(2).userId(2).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build());
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
		Winner before = localSqlSession.getMapper(WinnerMapper.class).findById(1);

		DefaultTransactionDefinition definition2 = new DefaultTransactionDefinition();
		definition2.setName("REPEATABLE_READ_READ_COMMITTED_UPDATE");
		definition2.setPropagationBehavior(3);
		TransactionStatus status2 = transactionManager.getTransaction(definition2);
		SqlSession localSqlSession2 = sqlSessionFactory.openSession();
		WinnerMapper localWinnerMapper = localSqlSession2.getMapper(WinnerMapper.class);
		localWinnerMapper.updateWinner(Winner.builder().id(1).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build());
		transactionManager.commit(status2);

		localSqlSession.clearCache();
		Winner after = localSqlSession.getMapper(WinnerMapper.class).findById(1);
		transactionManager.commit(status);
		Assertions.assertNotEquals(before.getWinner(), after.getWinner());
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
		Winner before = localSqlSession.getMapper(WinnerMapper.class).findById(1);

		DefaultTransactionDefinition definition2 = new DefaultTransactionDefinition();
		definition2.setName("REPEATABLE_READ_REPEATABLE_READ_UPDATE");
		definition2.setPropagationBehavior(3);
		TransactionStatus status2 = transactionManager.getTransaction(definition2);
		SqlSession localSqlSession2 = sqlSessionFactory.openSession();
		WinnerMapper localWinnerMapper = localSqlSession2.getMapper(WinnerMapper.class);
		localWinnerMapper.updateWinner(Winner.builder().id(1).userId(1).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build());
		transactionManager.commit(status2);

		localSqlSession.clearCache();
		Winner after = localSqlSession.getMapper(WinnerMapper.class).findById(1);
		transactionManager.commit(status);
		Assertions.assertEquals(before.getWinner(), after.getWinner());
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
		int beforeWinnerList = localSqlSession.getMapper(WinnerMapper.class).findByIdGtCount(0);

		DefaultTransactionDefinition definition2 = new DefaultTransactionDefinition();
		definition2.setName("REPEATABLE_READ_REPEATABLE_READ_INSERT_INSERT");
		definition2.setPropagationBehavior(3);
		definition2.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
		TransactionStatus status2 = transactionManager.getTransaction(definition2);
		SqlSession localSqlSession2 = sqlSessionFactory.openSession();
		WinnerMapper localWinnerMapper = localSqlSession2.getMapper(WinnerMapper.class);
		localWinnerMapper.insertWinner(Winner.builder().id(3).userId(3).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build());
		localWinnerMapper.insertWinner(Winner.builder().id(4).userId(4).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build());
		transactionManager.commit(status2);

		localSqlSession.clearCache();
		int afterWinnerList = localSqlSession.getMapper(WinnerMapper.class).findByIdGtCount(0);
		transactionManager.commit(status);
		Assertions.assertNotEquals(beforeWinnerList, afterWinnerList);
	}

	@Test
	@Order(8)
	@DisplayName("SERIALIZABLE_INSERT")
	public void serializable_read_insert() {
		DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
		definition.setName("SERIALIZABLE");
		definition.setPropagationBehavior(0);
		definition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		SqlSession localSqlSession = sqlSessionFactory.openSession();
		TransactionStatus status = transactionManager.getTransaction(definition);
		List<Winner> beforeWinnerList = localSqlSession.getMapper(WinnerMapper.class).findByIdGt(0);

		DefaultTransactionDefinition definition2 = new DefaultTransactionDefinition();
		definition2.setName("SERIALIZABLE_INSERT");
		definition2.setPropagationBehavior(3);
		TransactionStatus status2 = transactionManager.getTransaction(definition2);
		SqlSession localSqlSession2 = sqlSessionFactory.openSession();
		WinnerMapper localWinnerMapper = localSqlSession2.getMapper(WinnerMapper.class);
		localWinnerMapper.insertWinner(Winner.builder().id(5).userId(5).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build());
		localWinnerMapper.insertWinner(Winner.builder().id(6).userId(6).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build());
		transactionManager.commit(status2);

		localSqlSession.clearCache();
		List<Winner> afterWinnerList = localSqlSession.getMapper(WinnerMapper.class).findByIdGt(0);
		transactionManager.commit(status);
		Assertions.assertEquals(beforeWinnerList.size(), afterWinnerList.size());
	}
}