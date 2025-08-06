package com.genius.primavera.testcontainers.v4.examples;

import com.genius.primavera.testcontainers.v4.ContainerInfo;
import com.genius.primavera.testcontainers.v4.ContainerType;
import com.genius.primavera.testcontainers.v4.bean.BeanCreator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * External plugin example: ClickHouse bean creator
 * 
 * This creates a DataSource bean for ClickHouse containers
 * 
 * To use this plugin:
 * 1. Add this class to your project
 * 2. Add the following line to META-INF/services/com.genius.primavera.testcontainers.v4.bean.BeanCreator:
 *    com.genius.primavera.testcontainers.v4.examples.ClickHouseBeanCreator
 * 3. Make sure ClickHouse JDBC driver is in your dependencies:
 *    implementation 'com.clickhouse:clickhouse-jdbc:0.4.6'
 * 
 * The created DataSource can be injected like this:
 * 
 * @Autowired
 * @Qualifier("analytics")
 * private DataSource clickHouseDataSource;
 * 
 * @Test
 * void testClickHouseQuery() {
 *     JdbcTemplate jdbcTemplate = new JdbcTemplate(clickHouseDataSource);
 *     
 *     jdbcTemplate.execute("CREATE TABLE test_table (id UInt32, name String) ENGINE = Memory");
 *     jdbcTemplate.update("INSERT INTO test_table VALUES (1, 'test')");
 *     
 *     String result = jdbcTemplate.queryForObject(
 *         "SELECT name FROM test_table WHERE id = 1", String.class);
 *     
 *     assertEquals("test", result);
 * }
 */
public class ClickHouseBeanCreator implements BeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        HikariConfig config = new HikariConfig();
        
        // ClickHouse JDBC URL format
        String jdbcUrl = String.format("jdbc:clickhouse://%s:%d/%s",
            containerInfo.getHost(),
            containerInfo.getMappedPort(), // Use HTTP port (8123)
            containerInfo.getSpec().getDatabaseOrDefault());
        
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        config.setUsername(containerInfo.getSpec().getUsernameOrDefault());
        config.setPassword(containerInfo.getSpec().getPasswordOrDefault());
        
        // ClickHouse specific connection pool settings
        config.setPoolName(containerInfo.getName() + "-clickhouse-pool");
        config.setMaximumPoolSize(5);  // ClickHouse doesn't need many connections
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10000);  // 10 seconds
        config.setIdleTimeout(300000);      // 5 minutes
        config.setMaxLifetime(900000);      // 15 minutes
        
        // ClickHouse specific properties
        config.addDataSourceProperty("socket_timeout", "10000");
        config.addDataSourceProperty("connection_timeout", "10000");
        config.addDataSourceProperty("compress", "true");
        config.addDataSourceProperty("decompress", "true");
        
        return new HikariDataSource(config);
    }
    
    @Override
    public ContainerType getSupportedType() {
        // Note: You would need to extend ContainerType enum to include CLICKHOUSE
        return ContainerType.valueOf("CLICKHOUSE");
    }
}