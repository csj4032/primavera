package com.genius.primavera.config;

import com.genius.primavera.domain.typehandler.ArticleStatusTypeHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

@TestConfiguration
@MapperScan("com.genius.primavera.domain.mapper")
public class MyBatisTestConfiguration {

    @Container
    static MariaDBContainer<?> mysql = new MariaDBContainer<>(DockerImageName.parse("mariadb:11.4.7"))
            .withDatabaseName("primavera")
            .withUsername("primavera")
            .withPassword("primavera")
            .withInitScript("sql/init.sql")
            .withReuse(true);

    static {
        mysql.start();
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .url(mysql.getJdbcUrl())
                .username(mysql.getUsername())
                .password(mysql.getPassword())
                .driverClassName("org.mariadb.jdbc.Driver")
                .build();
    }

    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        // Register type handlers
        org.apache.ibatis.session.Configuration config = new org.apache.ibatis.session.Configuration();
        config.getTypeHandlerRegistry().register(ArticleStatusTypeHandler.class);
        sessionFactory.setConfiguration(config);
        
        return sessionFactory.getObject();
    }
}