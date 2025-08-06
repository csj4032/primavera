package com.genius.primavera.testcontainer.bean;

import com.genius.primavera.testcontainer.ContainerInfo;
import com.genius.primavera.testcontainer.ContainerType;

public interface BeanCreator {
    
    Object createBean(ContainerInfo containerInfo);
    
    ContainerType getSupportedType();
}