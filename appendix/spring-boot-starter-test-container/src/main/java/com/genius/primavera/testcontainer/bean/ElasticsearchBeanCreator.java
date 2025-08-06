package com.genius.primavera.testcontainer.bean;

import com.genius.primavera.testcontainer.ContainerInfo;
import com.genius.primavera.testcontainer.ContainerType;

import java.util.HashMap;
import java.util.Map;

public class ElasticsearchBeanCreator implements BeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        Map<String, Object> config = new HashMap<>();
        config.put("host", containerInfo.getHost());
        config.put("port", containerInfo.getMappedPort());
        config.put("scheme", "http");
        config.put("uris", containerInfo.getConnectionString());
        return config;
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.ELASTICSEARCH;
    }
}