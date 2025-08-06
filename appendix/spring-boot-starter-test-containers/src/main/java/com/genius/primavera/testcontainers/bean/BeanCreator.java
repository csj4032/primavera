package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;

public interface BeanCreator {
    
    Object createBean(ContainerInfo containerInfo);
    
    ContainerType getSupportedType();
}