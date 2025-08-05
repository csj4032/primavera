package com.genius.primavera.testcontainer.v3;

import org.testcontainers.containers.*;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;

/**
 * TestContainer 팩토리 (v3)
 * 
 * <p>설정에 따라 적절한 컨테이너 인스턴스를 생성합니다.</p>
 */
public class ContainerFactory {
    
    public static GenericContainer<?> create(ContainerType type, 
                                           TestContainerProperties.ContainerConfig config) {
        String image = config.getImage() != null ? config.getImage() : type.getDefaultImage();
        
        switch (type) {
            case MARIADB:
                return createMariaDB(image, config);
            case MYSQL:
                return createMySQL(image, config);
            case POSTGRESQL:
                return createPostgreSQL(image, config);
            case MONGODB:
                return createMongoDB(image, config);
            case REDIS:
                return createRedis(image, config);
            case KAFKA:
                return createKafka(image, config);
            case ELASTICSEARCH:
                return createElasticsearch(image, config);
            default:
                throw new IllegalArgumentException("Unsupported container type: " + type);
        }
    }
    
    private static MariaDBContainer<?> createMariaDB(String image, 
                                                   TestContainerProperties.ContainerConfig config) {
        MariaDBContainer<?> container = new MariaDBContainer<>(DockerImageName.parse(image))
            .withDatabaseName(config.getDatabase())
            .withUsername(config.getUsername())
            .withPassword(config.getPassword());
            
        if (config.getInitScript() != null && !config.getInitScript().isEmpty()) {
            container.withInitScript(config.getInitScript());
        }
        
        configureCommonSettings(container, config);
        return container;
    }
    
    private static MySQLContainer<?> createMySQL(String image, 
                                               TestContainerProperties.ContainerConfig config) {
        MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse(image))
            .withDatabaseName(config.getDatabase())
            .withUsername(config.getUsername())
            .withPassword(config.getPassword());
            
        if (config.getInitScript() != null && !config.getInitScript().isEmpty()) {
            container.withInitScript(config.getInitScript());
        }
        
        configureCommonSettings(container, config);
        return container;
    }
    
    private static PostgreSQLContainer<?> createPostgreSQL(String image, 
                                                         TestContainerProperties.ContainerConfig config) {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse(image))
            .withDatabaseName(config.getDatabase())
            .withUsername(config.getUsername())
            .withPassword(config.getPassword());
            
        if (config.getInitScript() != null && !config.getInitScript().isEmpty()) {
            container.withInitScript(config.getInitScript());
        }
        
        configureCommonSettings(container, config);
        return container;
    }
    
    private static MongoDBContainer createMongoDB(String image, 
                                                TestContainerProperties.ContainerConfig config) {
        MongoDBContainer container = new MongoDBContainer(DockerImageName.parse(image));
        configureCommonSettings(container, config);
        return container;
    }
    
    private static GenericContainer<?> createRedis(String image, 
                                                 TestContainerProperties.ContainerConfig config) {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(image))
            .withExposedPorts(6379);
            
        if (config.getPassword() != null && !config.getPassword().isEmpty()) {
            container.withCommand("redis-server", "--requirepass", config.getPassword());
        }
        
        configureCommonSettings(container, config);
        return container;
    }
    
    private static KafkaContainer createKafka(String image, 
                                            TestContainerProperties.ContainerConfig config) {
        KafkaContainer container = new KafkaContainer(DockerImageName.parse(image));
        configureCommonSettings(container, config);
        return container;
    }
    
    private static ElasticsearchContainer createElasticsearch(String image, 
                                                            TestContainerProperties.ContainerConfig config) {
        ElasticsearchContainer container = new ElasticsearchContainer(DockerImageName.parse(image));
        
        // 보안 비활성화 (테스트 환경)
        container.withEnv("xpack.security.enabled", "false");
        container.withEnv("discovery.type", "single-node");
        
        configureCommonSettings(container, config);
        return container;
    }
    
    /**
     * 공통 컨테이너 설정 적용
     */
    private static void configureCommonSettings(GenericContainer<?> container, 
                                               TestContainerProperties.ContainerConfig config) {
        // 환경 변수 설정
        for (Map.Entry<String, String> env : config.getEnvironment().entrySet()) {
            container.withEnv(env.getKey(), env.getValue());
        }
        
        // 네트워크 별칭 설정
        if (config.getNetworkAliases().length > 0) {
            container.withNetworkAliases(config.getNetworkAliases());
        }
        
        // 시작 타임아웃 설정
        container.withStartupTimeout(Duration.ofSeconds(config.getStartupTimeout()));
        
        // 포트 매핑은 제거 (TestContainers가 자동으로 할당)
        // 고정 포트가 필요한 경우 다른 방법 사용
    }
}