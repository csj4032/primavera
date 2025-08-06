package com.genius.primavera.testcontainer.bean;

import com.genius.primavera.testcontainer.ContainerInfo;
import com.genius.primavera.testcontainer.ContainerType;

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