package com.genius.primavera.testcontainers.bean;

import com.genius.primavera.testcontainers.ContainerInfo;
import com.genius.primavera.testcontainers.ContainerType;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

public class KafkaBeanCreator implements BeanCreator {
    
    @Override
    public Object createBean(ContainerInfo containerInfo) {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", containerInfo.getConnectionString());
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        props.put("acks", "all");
        props.put("retries", 3);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        
        ProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }
    
    @Override
    public ContainerType getSupportedType() {
        return ContainerType.KAFKA;
    }
}