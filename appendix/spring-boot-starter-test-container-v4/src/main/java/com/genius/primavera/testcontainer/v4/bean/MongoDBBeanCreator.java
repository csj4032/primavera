package com.genius.primavera.testcontainer.v4.bean;

import com.genius.primavera.testcontainer.v4.ContainerInfo;
import com.genius.primavera.testcontainer.v4.ContainerType;

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