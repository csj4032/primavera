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
        
        String databaseName = extractDatabaseName(connectionString);
        return new MongoTemplate(mongoClient, databaseName);
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MONGODB;
    }
    
    private String extractDatabaseName(String connectionString) {
        if (connectionString.contains("/") && connectionString.lastIndexOf("/") < connectionString.length() - 1) {
            String[] parts = connectionString.split("/");
            String lastPart = parts[parts.length - 1];
            return lastPart.contains("?") ? lastPart.split("\\?")[0] : lastPart;
        }
        return "test";
    }
}