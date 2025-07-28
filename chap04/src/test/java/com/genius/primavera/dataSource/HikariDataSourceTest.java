package com.genius.primavera.dataSource;

import com.genius.primavera.test.ContainerType;
import com.genius.primavera.test.EnablePrimaveraTestcontainers;
import com.genius.primavera.testContainer.MariaDBTestcontainerMixin;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.SQLException;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class HikariDataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testHikariDataSource() throws SQLException {
        log.info("DataSource class: {}", dataSource.getClass().getName());
        log.info("HikariDataSource class: {}", dataSource.getConnection().getCatalog());
    }
}