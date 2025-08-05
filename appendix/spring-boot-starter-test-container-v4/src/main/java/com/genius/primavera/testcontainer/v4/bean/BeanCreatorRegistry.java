package com.genius.primavera.testcontainer.v4.bean;

import com.genius.primavera.testcontainer.v4.ContainerType;

import java.util.HashMap;
import java.util.Map;

public class BeanCreatorRegistry {
    
    private static final Map<ContainerType, BeanCreator> creators = new HashMap<>();
    
    static {
        registerCreator(new DataSourceBeanCreator.MariaDBBeanCreator());
        registerCreator(new DataSourceBeanCreator.MySQLBeanCreator());
        registerCreator(new DataSourceBeanCreator.PostgreSQLBeanCreator());
        registerCreator(new RedisBeanCreator());
        registerCreator(new KafkaBeanCreator());
        registerCreator(new MongoDBBeanCreator());
        registerCreator(new ElasticsearchBeanCreator());
    }
    
    public static void registerCreator(BeanCreator creator) {
        creators.put(creator.getSupportedType(), creator);
    }
    
    public static BeanCreator getCreator(ContainerType type) {
        BeanCreator creator = creators.get(type);
        if (creator == null) {
            throw new IllegalArgumentException("No bean creator registered for container type: " + type);
        }
        return creator;
    }
    
    public static boolean isSupported(ContainerType type) {
        return creators.containsKey(type);
    }
}