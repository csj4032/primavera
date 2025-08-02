package com.genius.primavera.testcontainer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "primavera.testcontainers")
public class PrimaveraTestcontainersProperties {

    private ContainerLifecycleMode lifecycleMode = ContainerLifecycleMode.PER_CLASS;
    
    // 각 컨테이너별 설정
    private MariaDBConfig mariadb = new MariaDBConfig();
    private MySQLConfig mysql = new MySQLConfig();
    private PostgreSQLConfig postgresql = new PostgreSQLConfig();
    private RedisConfig redis = new RedisConfig();
    private KafkaConfig kafka = new KafkaConfig();
    private ElasticsearchConfig elasticsearch = new ElasticsearchConfig();

    // 하위 호환성을 위한 메서드
    public Map<String, ContainerConfig> getContainers() {
        Map<String, ContainerConfig> containers = new HashMap<>();
        containers.put("mariadb", mariadb);
        containers.put("mysql", mysql);
        containers.put("postgresql", postgresql);
        containers.put("redis", redis);
        containers.put("kafka", kafka);
        containers.put("elasticsearch", elasticsearch);
        return containers;
    }

    // 기본 컨테이너 설정 추상 클래스
    @Data
    public static abstract class ContainerConfig {
        private boolean enabled = true;
        private String dockerImageName;
        private Map<String, String> environment = new HashMap<>();
    }

    // 데이터베이스 컨테이너 공통 설정
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static abstract class DatabaseConfig extends ContainerConfig {
        private String driverClassName;
        private String databaseName;
        private String username;
        private String password;
        private String initScript;
    }

    // MariaDB 설정
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class MariaDBConfig extends DatabaseConfig {
        public MariaDBConfig() {
            setDockerImageName("mariadb:11.4.7");
            setDriverClassName("org.mariadb.jdbc.Driver");
            setDatabaseName("primavera");
            setUsername("primavera");
            setPassword("primavera");
        }
    }

    // MySQL 설정
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class MySQLConfig extends DatabaseConfig {
        public MySQLConfig() {
            setDockerImageName("mysql:8.0");
            setDriverClassName("com.mysql.cj.jdbc.Driver");
            setDatabaseName("primavera");
            setUsername("primavera");
            setPassword("primavera");
        }
    }

    // PostgreSQL 설정
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class PostgreSQLConfig extends DatabaseConfig {
        public PostgreSQLConfig() {
            setDockerImageName("postgres:15");
            setDriverClassName("org.postgresql.Driver");
            setDatabaseName("primavera");
            setUsername("primavera");
            setPassword("primavera");
        }
    }

    // Redis 설정
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class RedisConfig extends ContainerConfig {
        private String password;
        private int port = 6379;

        public RedisConfig() {
            setDockerImageName("redis:7.0");
        }
    }

    // Kafka 설정
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class KafkaConfig extends ContainerConfig {
        private String bootstrapServers;
        private int port = 9092;

        public KafkaConfig() {
            setDockerImageName("confluentinc/cp-kafka:latest");
        }
    }

    // Elasticsearch 설정
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ElasticsearchConfig extends ContainerConfig {
        private String clusterName = "elasticsearch";
        private int httpPort = 9200;
        private int transportPort = 9300;

        public ElasticsearchConfig() {
            setDockerImageName("elasticsearch:8.5.0");
        }
    }
}