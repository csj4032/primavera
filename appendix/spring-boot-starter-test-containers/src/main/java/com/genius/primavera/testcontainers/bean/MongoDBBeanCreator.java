package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.data.mongodb.core.MongoTemplate;

public class MongoDBBeanCreator implements BeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        String connectionString = containerInfo.getConnectionString();
        MongoClient mongoClient = MongoClients.create(connectionString);
        
        // 데이터베이스명을 연결 문자열에서 추출하거나 기본값 사용
        String databaseName = extractDatabaseName(connectionString);
        return new MongoTemplate(mongoClient, databaseName);
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MONGODB;
    }
    
    private String extractDatabaseName(String connectionString) {
        // mongodb://localhost:27017/testdb 형식에서 데이터베이스명 추출
        if (connectionString.contains("/") && connectionString.lastIndexOf("/") < connectionString.length() - 1) {
            String[] parts = connectionString.split("/");
            String lastPart = parts[parts.length - 1];
            // 쿼리 파라미터가 있으면 제거
            return lastPart.contains("?") ? lastPart.split("\\?")[0] : lastPart;
        }
        return "test"; // 기본 데이터베이스명
    }
}