package com.genius.primavera.testcontainer.v4.bean;

import com.genius.primavera.testcontainer.v4.ContainerInfo;
import com.genius.primavera.testcontainer.v4.ContainerType;

public interface BeanCreator {
    
    Object createBean(ContainerInfo containerInfo);
    
    ContainerType getSupportedType();
}