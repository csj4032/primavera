package com.genius.primavera.testContainer.strategy;

import com.genius.primavera.testContainer.ContainerType;
import com.genius.primavera.testContainer.config.KafkaContainerConfig;
import com.genius.primavera.testContainer.config.MariaDBContainerConfig;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.kafka.KafkaContainer;

@Slf4j
public class KafkaContainerStrategy extends AbstractContainerStrategy<KafkaContainer> {

    public KafkaContainerStrategy(KafkaContainerConfig config) {
        super(ContainerType.KAFKA, config);
    }
}