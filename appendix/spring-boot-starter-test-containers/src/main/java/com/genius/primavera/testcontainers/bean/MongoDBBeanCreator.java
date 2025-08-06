package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;

public class MongoDBBeanCreator implements BeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        return containerInfo.getConnectionString();
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.MONGODB;
    }
}