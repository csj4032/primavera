package com.genius.primavera.testcontainer.v4;

import com.genius.primavera.testcontainer.v4.creator.*;

import java.util.HashMap;
import java.util.Map;

public class ContainerCreatorRegistry {
    
    private static final Map<ContainerType, ContainerCreator> creators = new HashMap<>();
    
    static {
        registerCreator(new MariaDBContainerCreator());
        registerCreator(new MySQLContainerCreator());
        registerCreator(new PostgreSQLContainerCreator());
        registerCreator(new RedisContainerCreator());
        registerCreator(new MongoDBContainerCreator());
        registerCreator(new KafkaContainerCreator());
        registerCreator(new ElasticsearchContainerCreator());
    }
    
    public static void registerCreator(ContainerCreator creator) {
        creators.put(creator.getSupportedType(), creator);
    }
    
    public static ContainerCreator getCreator(ContainerType type) {
        ContainerCreator creator = creators.get(type);
        if (creator == null) {
            throw new IllegalArgumentException("No creator registered for container type: " + type);
        }
        return creator;
    }
    
    public static boolean isSupported(ContainerType type) {
        return creators.containsKey(type);
    }
}