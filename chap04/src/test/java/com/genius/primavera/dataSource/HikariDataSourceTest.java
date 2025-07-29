package com.genius.primavera.dataSource;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.EnablePrimaveraTestcontainers;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @ActiveProfiles("test")만 존재할 경우 Vault 연결 정보을 이용
 * @EnablePrimaveraTestcontainers 사용할 경우 testcontainers를 이용하여 MariaDB 컨테이너를 실행
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnablePrimaveraTestcontainers(ContainerType.MARIADB)
public class HikariDataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testHikariDataSource() throws SQLException {
        log.info("DataSource class: {}", dataSource.getClass().getName());
        log.info("HikariDataSource class: {}", dataSource.getConnection().getCatalog());
    }
}