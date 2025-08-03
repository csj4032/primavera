package com.genius.primavera.testcontainer.v2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "primavera.testcontainers")
public class TestContainerProperties {
    
    private ContainerLifecycleMode lifecycleMode = ContainerLifecycleMode.PER_METHOD;
    private MariaDBConfig mariadb = new MariaDBConfig();
    private MySQLConfig mysql = new MySQLConfig();
    private PostgreSQLConfig postgresql = new PostgreSQLConfig();
    private RedisConfig redis = new RedisConfig();
    private KafkaConfig kafka = new KafkaConfig();
    private ElasticsearchConfig elasticsearch = new ElasticsearchConfig();
    private MongoDBConfig mongodb = new MongoDBConfig();
    
    @Data
    public static class ContainerConfig {
        protected boolean enabled = true;
        protected String dockerImageName;
        protected String driverClassName;
        protected String databaseName;
        protected String username;
        protected String password;
        protected String initScript;
    }
    
    @Data
    public static class DatabaseConfig extends ContainerConfig {
        public DatabaseConfig(String defaultImage, String defaultDriver, String defaultDatabase) {
            this.dockerImageName = defaultImage;
            this.driverClassName = defaultDriver;
            this.databaseName = defaultDatabase;
            this.username = "primavera";
            this.password = "primavera";
            this.initScript = "sql/init.sql";
        }
    }
    
    @Data
    public static class MariaDBConfig extends DatabaseConfig {
        public MariaDBConfig() {
            super("mariadb:11.4.7", "org.mariadb.jdbc.Driver", "primavera");
        }
    }
    
    @Data
    public static class MySQLConfig extends DatabaseConfig {
        public MySQLConfig() {
            super("mysql:8.0", "com.mysql.cj.jdbc.Driver", "primavera");
        }
    }
    
    @Data
    public static class PostgreSQLConfig extends DatabaseConfig {
        public PostgreSQLConfig() {
            super("postgres:16", "org.postgresql.Driver", "primavera");
        }
    }
    
    @Data
    public static class RedisConfig extends ContainerConfig {
        public RedisConfig() {
            this.dockerImageName = "redis:7-alpine";
        }
    }
    
    @Data
    public static class KafkaConfig extends ContainerConfig {
        public KafkaConfig() {
            this.dockerImageName = "confluentinc/cp-kafka:latest";
        }
    }
    
    @Data
    public static class ElasticsearchConfig extends ContainerConfig {
        public ElasticsearchConfig() {
            this.dockerImageName = "elasticsearch:8.11.0";
        }
    }
    
    @Data
    public static class MongoDBConfig extends ContainerConfig {
        public MongoDBConfig() {
            this.dockerImageName = "mongo:7";
            this.databaseName = "primavera";
            this.username = "primavera";
            this.password = "primavera";
        }
    }
}