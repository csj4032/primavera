package com.genius.primavera.application;

import com.genius.primavera.domain.mapper.WinnerMapper;
import com.genius.primavera.domain.model.Winner;
import com.genius.primavera.domain.model.WinnerType;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static java.sql.Connection.TRANSACTION_READ_COMMITTED;
import static java.sql.Connection.TRANSACTION_REPEATABLE_READ;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DisplayName(value = "트랜잭션 Isolation 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WinnerServiceIsolationTest {

	private static SqlSession sqlSession;

	@Autowired
	private SqlSessionFactory sqlSessionFactory;

	@Autowired
	private WinnerService winnerService;

	@Test
	@Order(1)
	@DisplayName("READ_UNCOMMITTED_INSERT")
	public void read_uncommitted_insert() {
		sqlSession = sqlSessionFactory.openSession();
		WinnerMapper winnerMapper = sqlSession.getMapper(WinnerMapper.class);
		winnerMapper.truncate();
		winnerMapper.insertWinner(Winner.builder().userId(1).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build());
	}

	@Test
	@Order(2)
	@DisplayName("READ_COMMITTED_SELECT")
	public void read_committed() {
		Assertions.assertTrue(winnerService.findAllCommitted().isEmpty());
	}

	@Test
	@Order(3)
	@DisplayName("READ_UNCOMMITTED_SELECT")
	public void read_uncommitted() {
		Assertions.assertTrue(!winnerService.findAllUncommitted().isEmpty());
	}

	@Test
	@Order(4)
	@DisplayName("READ_UNCOMMITTED_COMMIT")
	public void read_uncommitted_commit() {
		sqlSession.commit();
	}

	@Test
	@Order(5)
	@DisplayName("REPEATABLE_READ_READ_COMMITTED")
	public void read_committed_repeatable() throws SQLException {
		sqlSession = sqlSessionFactory.openSession();
		sqlSession.getConnection().setTransactionIsolation(TRANSACTION_READ_COMMITTED);
		Winner before = sqlSession.getMapper(WinnerMapper.class).findById(1);
		sqlSession.clearCache();

		SqlSession localSqlSession = sqlSessionFactory.openSession();
		WinnerMapper localWinnerMapper = localSqlSession.getMapper(WinnerMapper.class);
		localWinnerMapper.updateWinner(Winner.builder().id(1).userId(1).winner(WinnerType.WINNER).regDt(LocalDateTime.now()).build());
		localSqlSession.commit();

		Winner after = sqlSession.getMapper(WinnerMapper.class).findById(1);

		Assertions.assertNotEquals(before.getWinner(), after.getWinner());
	}

	@Test
	@Order(6)
	@DisplayName("REPEATABLE_READ_REPEATABLE_READ")
	public void repeatable_read_select() throws SQLException {
		sqlSession = sqlSessionFactory.openSession();
		sqlSession.getConnection().setTransactionIsolation(TRANSACTION_REPEATABLE_READ);
		Winner before = sqlSession.getMapper(WinnerMapper.class).findById(1);
		sqlSession.clearCache();

		SqlSession localSqlSession = sqlSessionFactory.openSession();
		WinnerMapper localWinnerMapper = localSqlSession.getMapper(WinnerMapper.class);
		localWinnerMapper.updateWinner(Winner.builder().id(1).userId(1).winner(WinnerType.LOSER).regDt(LocalDateTime.now()).build());
		localSqlSession.commit();

		Winner after = sqlSession.getMapper(WinnerMapper.class).findById(1);

		Assertions.assertEquals(before.getWinner(), after.getWinner());
	}
}